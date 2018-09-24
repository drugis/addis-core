package org.drugis.trialverse.graph.service.impl;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.client.utils.URIBuilder;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.AuthenticationService;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.trialverse.dataset.exception.RevisionNotFoundException;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.repository.DatasetReadRepository;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.graph.service.GraphService;
import org.drugis.trialverse.security.TrialversePrincipal;
import org.drugis.trialverse.util.Namespaces;
import org.drugis.addis.util.WebConstants;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by daan on 26-8-15.
 */
@Service
public class GraphServiceImpl implements GraphService {

  private static final String REVISION = "revision";
  private static final String COPY_MESSAGE = "Study copied from other dataset";

  @Inject
  private AuthenticationService authenticationService;

  @Inject
  private DatasetReadRepository datasetReadRepository;

  @Inject
  private VersionMappingRepository versionMappingRepository;

  @Inject
  private AccountRepository accountRepository;

  @Inject
  private RestTemplate restTemplate;

  @Inject
  private WebConstants webConstants;

  private Pattern graphUriPattern = Pattern.compile("/datasets/([a-zA-z0-9\\.\\-]*)/versions/([a-zA-z0-9\\.\\-]*)/graphs/([a-zA-z0-9\\.\\-]*)");

  @Override
  public URI extractDatasetUri(URI sourceGraphUri) {
    Matcher matcher = graphUriPattern.matcher(sourceGraphUri.getPath());
    matcher.matches();
    String datasetUuid = matcher.group(1);
    return URI.create(Namespaces.DATASET_NAMESPACE + datasetUuid);
  }

  @Override
  public URI extractVersionUri(URI sourceGraphUri) {
    Matcher matcher = graphUriPattern.matcher(sourceGraphUri.getPath());
    matcher.matches();
    String versionUuid = matcher.group(2);
    return WebConstants.buildVersionUri(versionUuid);
  }

  @Override
  public URI extractGraphUri(URI sourceGraphUri) {
    Matcher matcher = graphUriPattern.matcher(sourceGraphUri.getPath());
    matcher.matches();
    String graphUuid = matcher.group(3);
    return buildGraphUri(graphUuid);
  }

  @Override
  public URI buildGraphUri(String graphUuid) {
    try {
      URIBuilder builder = new URIBuilder(Namespaces.BASE_NAMESPACE);
      builder.setPath("/graphs/" + graphUuid);
      return builder.build();
    } catch (URISyntaxException e) {
      throw new RuntimeException("failed to build uri" + e.getMessage());
    }
  }

  @Override
  @CacheEvict(cacheNames = "datasetHistory", key="#targetDatasetUri.toString()")
  public URI copy(URI targetDatasetUri, URI targetGraphUri, URI copyOfUri) throws URISyntaxException, IOException, RevisionNotFoundException {

    URI trialverseDatasetUri = extractDatasetUri(copyOfUri);
    VersionMapping sourceDatasetMapping = versionMappingRepository.getVersionMappingByDatasetUrl(trialverseDatasetUri);
    Model historyModel = datasetReadRepository.getHistory(sourceDatasetMapping.getVersionedDatasetUri());

    URI revisionUri = getRevisionUri(historyModel, copyOfUri);

    URI uri = UriComponentsBuilder.fromHttpUrl(targetDatasetUri.toString())
            .path(WebConstants.DATA_ENDPOINT)
            .queryParam(WebConstants.COPY_OF_QUERY_PARAM, revisionUri.toString())
            .queryParam(WebConstants.GRAPH_QUERY_PARAM, targetGraphUri.toString())
            .build()
            .toUri();

    HttpHeaders headers = new HttpHeaders();
    TrialversePrincipal owner = authenticationService.getAuthentication();
    Account currentUserAccount = accountRepository.findAccountByUsername(owner.getUserName());

    if(owner.hasApiKey()) {
      headers.add(WebConstants.EVENT_SOURCE_CREATOR_HEADER, "https://trialverse.org/apikeys/" + owner.getApiKey().getId());
    } else {
      headers.add(WebConstants.EVENT_SOURCE_CREATOR_HEADER, "mailto:" + currentUserAccount.getEmail());
    }
    headers.add(WebConstants.EVENT_SOURCE_TITLE_HEADER, Base64.encodeBase64String(COPY_MESSAGE.getBytes()));
    ResponseEntity response = restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(headers), String.class);
    List<String> newVersion = response.getHeaders().get(WebConstants.X_EVENT_SOURCE_VERSION);

    return new URI(newVersion.get(0));
  }

  @Override
  public InputStream jsonGraphInputStreamToTurtleInputStream(InputStream jsonGraph) {
    Model model = ModelFactory.createDefaultModel();
    model.read(jsonGraph, null, RDFLanguages.strLangJSONLD);

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    RDFDataMgr.write(outputStream, model, Lang.TURTLE) ;
    byte[] bytes = outputStream.toByteArray();
    return new ByteArrayInputStream(bytes);
  }

  private URI getRevisionUri(Model historyModel, URI sourceGraphUri) throws URISyntaxException, RevisionNotFoundException, IOException {
    String revisionSparqlTemplate = IOUtils.toString(new ClassPathResource("getRevision.sparql").getInputStream(), "UTF-8");

    String sourceVersion = extractVersionUri(sourceGraphUri).toString();
    String sourceGraph = extractGraphUri(sourceGraphUri).toString();
    revisionSparqlTemplate = revisionSparqlTemplate
            .replace("$sourceVersion", sourceVersion)
            .replace("$sourceGraph", sourceGraph);

    Query query = QueryFactory.create(revisionSparqlTemplate);
    QueryExecution queryExecution = QueryExecutionFactory.create(query, historyModel);
    ResultSet resultSet = queryExecution.execSelect();

    if (!resultSet.hasNext()) {
      throw new RevisionNotFoundException("Unable to find revision for version " + sourceVersion + ", graph " + sourceGraph);
    }
    QuerySolution solution = resultSet.nextSolution();
    RDFNode revision = solution.get(REVISION);
    if(resultSet.hasNext()) {
      throw new RevisionNotFoundException("Too many revisions found" + sourceVersion + ", graph " + sourceGraph);
    }
    queryExecution.close();
    return new URI(revision.asNode().getURI());
  }
}



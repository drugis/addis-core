package org.drugis.trialverse.graph.service.impl;

import org.apache.commons.io.IOUtils;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.drugis.trialverse.dataset.exception.RevisionNotFoundException;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.repository.DatasetReadRepository;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.graph.service.GraphService;
import org.drugis.trialverse.util.WebConstants;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.inject.Inject;
import java.io.IOException;
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

  public static final String REVISION = "revision";

  @Inject
  private DatasetReadRepository datasetReadRepository;

  @Inject
  private VersionMappingRepository versionMappingRepository;

  @Inject
  private RestTemplate restTemplate;

  private Pattern graphUriPattern = Pattern.compile(".*://[a-zA-z0-9\\.]*/datasets/([a-zA-z0-9\\.\\-]*)/versions/([a-zA-z0-9\\.\\-]*)/graphs/([a-zA-z0-9\\.\\-]*)");

  @Override
  public String extractDatasetUuid(String sourceGraphUri) {
    Matcher matcher = graphUriPattern.matcher(sourceGraphUri);
    matcher.matches();
    return matcher.group(1);
  }
  @Override
  public String extractVersionUuid(String sourceGraphUri) {
    Matcher matcher = graphUriPattern.matcher(sourceGraphUri);
    matcher.matches();
    return matcher.group(2);
  }
  @Override
  public String extractGraphUuid(String sourceGraphUri) {
    Matcher matcher = graphUriPattern.matcher(sourceGraphUri);
    matcher.matches();
    return matcher.group(3);
  }

  @Override
  public URI copy(URI targetDatasetUri, URI targetGraphUri, URI sourceDatasetUri, URI sourceVersionUri, URI sourceGraphUri) throws URISyntaxException, IOException, RevisionNotFoundException {
    VersionMapping targetDatasetMapping = versionMappingRepository.getVersionMappingByDatasetUrl(targetDatasetUri);
    Model historyModel = datasetReadRepository.getHistory(sourceDatasetUri);
    URI revisionUri = getRevisionUri(historyModel, sourceVersionUri, sourceGraphUri);

    URI uri = UriComponentsBuilder.fromHttpUrl(targetDatasetMapping.getVersionedDatasetUrl())
            .path(WebConstants.DATA_ENDPOINT)
            .queryParam(WebConstants.COPY_OF_QUERY_PARAM, revisionUri.toString())
            .queryParam(WebConstants.GRAPH_QUERY_PARAM, targetGraphUri.toString())
            .build()
            .toUri();
    HttpEntity requestEntity = new HttpEntity<>(null);

    ResponseEntity response = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String.class);
    List<String> newVersion = response.getHeaders().get(WebConstants.X_EVENT_SOURCE_VERSION);

    return new URI(newVersion.get(0));
  }

  private URI getRevisionUri(Model historyModel, URI sourceVersionUri, URI sourceGraphUri) throws URISyntaxException, RevisionNotFoundException, IOException {
    String revisionSparqlTemplate = IOUtils.toString(new ClassPathResource("getRevision.sparql").getInputStream(), "UTF-8");

    revisionSparqlTemplate = revisionSparqlTemplate
            .replace("$sourceVersion", sourceVersionUri.toString())
            .replace("$sourceGraph", sourceGraphUri.toString());

    Query query = QueryFactory.create(revisionSparqlTemplate);
    QueryExecution queryExecution = QueryExecutionFactory.create(query, historyModel);
    ResultSet resultSet = queryExecution.execSelect();

    if (!resultSet.hasNext()) {
      throw new RevisionNotFoundException("Unable to find revision for version " + sourceVersionUri.toString() + ", graph " + sourceGraphUri.toString());
    }
    QuerySolution solution = resultSet.nextSolution();
    RDFNode revision = solution.get(REVISION);
    if(resultSet.hasNext()) {
      throw new RevisionNotFoundException("Too many revisions found" + sourceVersionUri.toString() + ", graph " + sourceGraphUri.toString());
    }
    queryExecution.close();
    return new URI(revision.asNode().getURI());
  }
}

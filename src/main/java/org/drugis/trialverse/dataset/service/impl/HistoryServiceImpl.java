package org.drugis.trialverse.dataset.service.impl;

import com.jayway.jsonpath.JsonPath;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.drugis.trialverse.dataset.exception.RevisionNotFoundException;
import org.drugis.trialverse.dataset.model.Merge;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.model.VersionNode;
import org.drugis.trialverse.dataset.model.VersionNodeBuilder;
import org.drugis.trialverse.dataset.repository.DatasetReadRepository;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.dataset.service.HistoryService;
import org.drugis.trialverse.graph.repository.GraphReadRepository;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.ApiKey;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.security.repository.ApiKeyRepository;
import org.drugis.trialverse.util.JenaProperties;
import org.drugis.trialverse.util.WebConstants;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Created by daan on 2-9-15.
 */
@Service
public class HistoryServiceImpl implements HistoryService {

  @Inject
  private ApiKeyRepository apiKeyRepository;

  @Inject
  private AccountRepository accountRepository;

  @Inject
  private VersionMappingRepository versionMappingRepository;

  @Inject
  private GraphReadRepository graphReadRepository;

  @Inject
  private DatasetReadRepository datasetReadRepository;

  @Inject
  private RestTemplate restTemplate;

  @Override
  public List<VersionNode> createHistory(URI trialverseDatasetUri) throws URISyntaxException, IOException, RevisionNotFoundException {
    VersionMapping versionMapping = versionMappingRepository.getVersionMappingByDatasetUrl(trialverseDatasetUri);
    Model historyModel = datasetReadRepository.getHistory(versionMapping.getVersionedDatasetUri());

    ResIterator stmtIterator = historyModel.listSubjectsWithProperty(JenaProperties.typeProperty, JenaProperties.datasetVersionObject);

    Map<String, Resource> versionMap = new HashMap<>();
    Map<String, Boolean> referencedMap = new HashMap<>();
    // create version map
    populateVersionMaps(stmtIterator, versionMap, referencedMap);

    // find head version
    Resource headVersion = null;
    for (String key : versionMap.keySet()) {
      if (referencedMap.get(key) == null) {
        headVersion = versionMap.get(key);
      }
    }

    // sort the versions
    List<VersionNode> sortedVersions = createSortedVersionList(versionMap, headVersion);

    Set<RDFNode> seenRevisions = new HashSet<>();
    for (VersionNode version : sortedVersions) {
      StmtIterator graphRevisionBlankNodes = historyModel.listStatements(historyModel.getResource(version.getUri()), JenaProperties.graphRevisionProperty, (RDFNode) null);

      while (graphRevisionBlankNodes.hasNext()) {
        Resource graphRevisionBlankNode = graphRevisionBlankNodes.nextStatement().getObject().asResource();
        StmtIterator revisionItr = historyModel.listStatements(graphRevisionBlankNode, JenaProperties.revisionProperty, (RDFNode) null);
        Resource revisionSubject = revisionItr.next().getObject().asResource();
        StmtIterator stmtIterator1 = historyModel.listStatements(revisionSubject, JenaProperties.mergedRevisionProperty, (RDFNode) null);
        if (stmtIterator1.hasNext()) { // it's a merge revision
          Statement mergedRevision = stmtIterator1.next();
          if (!seenRevisions.contains(mergedRevision.getObject())) { // that hasn't been seen before
            seenRevisions.add(mergedRevision.getObject());
            StmtIterator datasetReference = historyModel.listStatements(mergedRevision.getResource(), JenaProperties.datasetProperty, (RDFNode) null);
            RDFNode sourceDataset = datasetReference.next().getObject();
            Merge merge = resolveMerge(mergedRevision.getObject().toString(), sourceDataset.toString());
            version.setMerge(merge);
          }
        }
      }
    }
    return sortedVersions;
  }

  private List<VersionNode> createSortedVersionList(Map<String, Resource> versionMap, Resource headVersion) {
    List<VersionNode> sortedVersions = new ArrayList<>();
    Resource current = headVersion;
    for (int historyOrder = 0; historyOrder < versionMap.size(); ++historyOrder) {
      sortedVersions.add(buildNewVersionNode(current, historyOrder));
      Resource next = current.getPropertyResourceValue(JenaProperties.previousProperty);
      current = next;
    }
    sortedVersions = Lists.reverse(sortedVersions);
    return sortedVersions;
  }

  private VersionNode buildNewVersionNode(Resource current, int historyOrder) {
    Statement title = current.getProperty(JenaProperties.TITLE_PROPERTY);
    Resource creatorProp = current.getPropertyResourceValue(JenaProperties.creatorProperty);
    String creator = creatorProp == null ? "unknown creator" : creatorProp.toString();
    ApiKey apiKey = null;
    Account account;
    if (creator.startsWith(WebConstants.API_KEY_PREFIX)) {
      apiKey = apiKeyRepository.get(Integer.valueOf(creator.substring(WebConstants.API_KEY_PREFIX.length())));
      account = accountRepository.findAccountById(apiKey.getAccountId());
    } else {
      account = accountRepository.findAccountByEmail(creator.substring("mailto:".length()));
    }
    creator = account.getFirstName() + " " + account.getLastName();
    Statement descriptionStatement = current.getProperty(JenaProperties.DESCRIPTION_PROPERTY);
    Statement dateProp = current.getProperty(JenaProperties.DATE_PROPERTY);
    return new VersionNodeBuilder()
            .setUri(current.getURI())
            .setVersionTitle(title == null ? "" : title.getObject().toString())
            .setVersionDate(((XSDDateTime) dateProp.getObject().asLiteral().getValue()).asCalendar().getTime())
            .setDescription(descriptionStatement == null ? null : descriptionStatement.getObject().toString())
            .setHistoryOrder(historyOrder)
            .setCreator(creator)
            .setUserId(account.getId())
            .setApplicationName(apiKey == null ? null : apiKey.getApplicationName())
            .build();
  }

  private void populateVersionMaps(ResIterator stmtIterator, Map<String, Resource> versionMap, Map<String, Boolean> referencedMap) {
    while (stmtIterator.hasNext()) {
      Resource resource = stmtIterator.nextResource();
      versionMap.put(resource.getURI(), resource);

      Resource previous = resource.getPropertyResourceValue(JenaProperties.previousProperty);
      if (previous != null) {
        referencedMap.put(previous.getURI(), true);
      }
    }
  }

  private Merge resolveMerge(String revisionUri, String sourceDatasetUri) throws IOException, URISyntaxException, RevisionNotFoundException {
    VersionMapping mapping = versionMappingRepository.getVersionMappingByVersionedURl(URI.create(sourceDatasetUri));
    Model history = datasetReadRepository.getHistory(URI.create(sourceDatasetUri));
    Pair<String, String> versionAndGraph = getVersionAndGraph(history, revisionUri);
    String userUuid = DigestUtils.sha256Hex(mapping.getOwnerUuid());
    String version = versionAndGraph.getLeft();
    String graph = versionAndGraph.getRight();
    String title = getStudyTitle(sourceDatasetUri, version, graph);
    // find graph and version for the merge revision
    return new Merge(revisionUri, mapping.getTrialverseDatasetUrl(), version, graph, title, userUuid);
  }

  private String getStudyTitle(String sourceDatasetUri, String version, String graph) throws IOException {
    String template = IOUtils.toString(new ClassPathResource("getGraphTitle.sparql")
            .getInputStream(), "UTF-8");
    String query = template.replace("$graphUri", graph);
    ResponseEntity<String> response = executeVersionedQuery(sourceDatasetUri, version, query);
    return JsonPath.read(response.getBody(), "$.results.bindings[0].$title.$value");
  }

  private ResponseEntity<String> executeVersionedQuery(String sourceDatasetUri, String version, String query) {
    UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(sourceDatasetUri)
            .path(WebConstants.QUERY_ENDPOINT)
            .queryParam(WebConstants.QUERY_PARAM_QUERY, query)
            .build();
    HttpHeaders headers = new HttpHeaders();
    headers.put(WebConstants.X_ACCEPT_EVENT_SOURCE_VERSION, Collections.singletonList(version));
    headers.put(WebConstants.ACCEPT_HEADER, Collections.singletonList(WebConstants.APPLICATION_SPARQL_RESULTS_JSON));

    return restTemplate.exchange(uriComponents.toUri(), HttpMethod.GET, new HttpEntity<>(headers), String.class);
  }

  private Pair<String, String> getVersionAndGraph(Model historyModel, String revisionUri) throws URISyntaxException,
          RevisionNotFoundException, IOException {
    String template = IOUtils.toString(new ClassPathResource("getGraphAndVersionByRevision.sparql")
            .getInputStream(), "UTF-8");
    template = template.replace("$revision", revisionUri);

    Query query = QueryFactory.create(template);
    QueryExecution queryExecution = QueryExecutionFactory.create(query, historyModel);
    ResultSet resultSet = queryExecution.execSelect();

    if (!resultSet.hasNext()) {
      throw new RevisionNotFoundException("Unable to find version and graph for revision " + revisionUri);
    }
    QuerySolution solution = resultSet.nextSolution();
    String version = solution.get(JenaProperties.VERSION).toString();
    String graph = solution.get(JenaProperties.GRAPH).toString();
    queryExecution.close();
    return Pair.of(version, graph);
  }
}

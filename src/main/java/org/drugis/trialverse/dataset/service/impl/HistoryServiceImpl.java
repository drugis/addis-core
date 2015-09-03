package org.drugis.trialverse.dataset.service.impl;

import com.jayway.jsonpath.JsonPath;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.drugis.trialverse.dataset.exception.RevisionNotFoundException;
import org.drugis.trialverse.dataset.model.Merge;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.model.VersionNode;
import org.drugis.trialverse.dataset.repository.DatasetReadRepository;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.dataset.service.HistoryService;
import org.drugis.trialverse.graph.repository.GraphReadRepository;
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
  private VersionMappingRepository versionMappingRepository;

  @Inject
  private GraphReadRepository graphReadRepository;

  @Inject
  private DatasetReadRepository datasetReadRepository;

  @Inject
  private RestTemplate restTemplate;

  private static final String GRAPH_REVISION = "graph_revision";
  private static final String MERGED_REVISION = "merged_revision";
  private static final String REVISION = "revision";
  private static final String VERSION = "version";
  private static final String GRAPH = "graph";
  private static final String DATASET = "dataset";
  private static final String DATASET_VERSION = "DatasetVersion";
  private static final String PREVIOUS = "previous";
  private static final String RDF_TYPE_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
  private static final String DC_TITLE = "http://purl.org/dc/terms/title";
  private static final String DC_CREATOR = "http://purl.org/dc/terms/creator";

  private static final Model defaultModel = ModelFactory.createDefaultModel();
  private static final String esPrefix = "http://drugis.org/eventSourcing/es#";
  private static final Property typeProperty = defaultModel.getProperty(RDF_TYPE_URI);
  private static final Property titleProperty = defaultModel.getProperty(DC_TITLE);
  private static final Property creatorProperty = defaultModel.getProperty(DC_CREATOR);
  private static final Property datasetVersionObject = defaultModel.getProperty(esPrefix, DATASET_VERSION);
  private static final Property previousProperty = defaultModel.getProperty(esPrefix, PREVIOUS);
  private static final Property mergedRevisionProperty = defaultModel.getProperty(esPrefix, MERGED_REVISION);
  private static final Property graphRevisionProperty = defaultModel.getProperty(esPrefix, GRAPH_REVISION);
  private static final Property revisionProperty = defaultModel.getProperty(esPrefix, REVISION);
  private static final Property datasetProperty = defaultModel.getProperty(esPrefix, DATASET);

  private Map<String, VersionNode> versionNodes = new HashMap<>();

  @Override
  public List<VersionNode> createHistory(URI trialverseDatasetUri) throws URISyntaxException, IOException, RevisionNotFoundException {
    VersionMapping versionMapping = versionMappingRepository.getVersionMappingByDatasetUrl(trialverseDatasetUri);
    Model historyModel = datasetReadRepository.getHistory(versionMapping.getVersionedDatasetUri());

    ResIterator stmtIterator = historyModel.listSubjectsWithProperty(typeProperty, datasetVersionObject);

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
    List<Resource> sortedVersions = sortVersions(versionMap, headVersion);

    Set<RDFNode> seenRevisions = new HashSet<>();
    for (Resource version : sortedVersions) {
      StmtIterator graphRevisionBlankNodes = historyModel.listStatements(version, graphRevisionProperty, (RDFNode) null);

      while (graphRevisionBlankNodes.hasNext()) {
        Resource graphRevisionBlankNode = graphRevisionBlankNodes.nextStatement().getObject().asResource();
        StmtIterator revisionItr = historyModel.listStatements(graphRevisionBlankNode, revisionProperty, (RDFNode) null);
        Resource revisionSubject = revisionItr.next().getObject().asResource();
        StmtIterator stmtIterator1 = historyModel.listStatements(revisionSubject, mergedRevisionProperty, (RDFNode) null);
        if (stmtIterator1.hasNext()) { // it's a merge revision
          Statement mergedRevision = stmtIterator1.next();
          if (!seenRevisions.contains(mergedRevision.getObject())) { // that hasn't been seen before
            seenRevisions.add(mergedRevision.getObject());
            StmtIterator datasetReference = historyModel.listStatements(mergedRevision.getResource(), datasetProperty, (RDFNode) null);
            RDFNode sourceDataset = datasetReference.next().getObject();
            Merge merge = resolveMerge(mergedRevision.getObject().toString(), sourceDataset.toString());
            versionNodes.get(version.getURI()).setMerge(merge);
          }
        }
      }
    }
    return new ArrayList<>(versionNodes.values());
  }

  private List<Resource> sortVersions(Map<String, Resource> versionMap, Resource headVersion) {
    List<Resource> sortedVersions = new ArrayList<>();
    Resource current = headVersion;
    for (int i = 0; i < versionMap.size(); ++i) {
      sortedVersions.add(current);
      RDFNode title = current.getProperty(titleProperty).getObject();
      String versionTitle = title == null ? "" : title.toString();
      Resource creatorProp = current.getPropertyResourceValue(creatorProperty);
      String creator = creatorProp == null ? "unknown creator" : creatorProp.toString();
      versionNodes.put(current.getURI(), new VersionNode(current.getURI(), versionTitle, creator, i));
      Resource next = current.getPropertyResourceValue(previousProperty);
      current = next;
    }
    sortedVersions = Lists.reverse(sortedVersions);
    return sortedVersions;
  }

  private void populateVersionMaps(ResIterator stmtIterator, Map<String, Resource> versionMap, Map<String, Boolean> referencedMap) {
    while (stmtIterator.hasNext()) {
      Resource resource = stmtIterator.nextResource();
      versionMap.put(resource.getURI(), resource);

      Resource previous = resource.getPropertyResourceValue(previousProperty);
      if (previous != null) {
        referencedMap.put(previous.getURI(), true);
      }
    }
  }

  private Merge resolveMerge(String revisionUri, String sourceDatasetUri) throws IOException, URISyntaxException, RevisionNotFoundException {
    Model history = datasetReadRepository.getHistory(URI.create(sourceDatasetUri));
    Pair<String, String> versionAndGraph = getVersionAndGraph(history, revisionUri);
    String version = versionAndGraph.getLeft();
    String graph = versionAndGraph.getRight();
    String title = getStudyTitle(sourceDatasetUri, version, graph);

    // find graph and version for the merge revision
    return new Merge(revisionUri, sourceDatasetUri, version, graph, title);
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
    String version = solution.get(VERSION).toString();
    String graph = solution.get(GRAPH).toString();
    queryExecution.close();
    return Pair.of(version, graph);
  }
}

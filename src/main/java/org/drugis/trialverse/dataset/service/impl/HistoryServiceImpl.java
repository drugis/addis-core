package org.drugis.trialverse.dataset.service.impl;

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
import org.drugis.trialverse.util.service.JenaQueryService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

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
  private JenaQueryService jenaQueryService;

  private static final String GRAPH_REVISION = "graph_revision";
  private static final String MERGED_REVISION = "merged_revision";
  private static final String REVISION = "revision";
  private static final String VERSION = "version";
  private static final String GRAPH = "graph";
  private static final String DATASET = "dataset";
  private  static final String DATASET_VERSION = "DatasetVersion";
  private static final String PREVIOUS = "previous";
  private final String RDF_TYPE_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
  private static final String DC_TITLE = "http://purl.org/dc/terms/title";
  private static final String DC_CREATOR = "http://purl.org/dc/terms/creator";

  private Map<String, VersionNode> versionNodes = new HashMap<>();

  @Override
  public List<VersionNode> createHistory(URI trialverseDatasetUri) throws URISyntaxException, IOException, RevisionNotFoundException {
    VersionMapping versionMapping = versionMappingRepository.getVersionMappingByDatasetUrl(trialverseDatasetUri);
    Model historyModel = datasetReadRepository.getHistory(versionMapping.getVersionedDatasetUri());

    String esPrefix = historyModel.getNsPrefixURI("es");
    Property typeProperty = historyModel.getProperty(RDF_TYPE_URI);
    Property titleProperty = historyModel.getProperty(DC_TITLE);
    Property creatorProperty = historyModel.getProperty(DC_CREATOR);
    RDFNode datasetVersionObject = historyModel.getProperty(esPrefix, DATASET_VERSION);
    ResIterator stmtIterator = historyModel.listSubjectsWithProperty(typeProperty, datasetVersionObject);
    Property previousProperty = historyModel.createProperty(esPrefix, PREVIOUS);
    Property mergedRevisionProperty = historyModel.getProperty(esPrefix, MERGED_REVISION);
    Property graphRevisionProperty = historyModel.getProperty(esPrefix, GRAPH_REVISION);
    Property revisionProperty = historyModel.getProperty(esPrefix, REVISION);
    Property datasetProperty = historyModel.getProperty(esPrefix, DATASET);

    Map<String, Resource> versionMap = new HashMap<>();
    Map<String, Boolean> referencedMap = new HashMap<>();
    // create version map
    while (stmtIterator.hasNext()) {
      Resource resource = stmtIterator.nextResource();
      versionMap.put(resource.getURI(), resource);

      Resource previous = resource.getPropertyResourceValue(previousProperty);
      if (previous != null) {
        referencedMap.put(previous.getURI(), true);
      }
      System.out.println(previous);
    }

    // find head version
    Resource headVersion = null;
    for (String key : versionMap.keySet()) {
      if (referencedMap.get(key) == null) {
        headVersion = versionMap.get(key);
      }
    }

    // sort the versions
    List<Resource> sortedVersions = new ArrayList<>();
    Resource current = headVersion;
    for(int i = 0; i < versionMap.size(); ++i) {
      sortedVersions.add(current);
      Resource title = current.getPropertyResourceValue(titleProperty);
      String versionTitle = title == null ? "" : title.toString();
      Resource creatorProp = current.getPropertyResourceValue(creatorProperty);
      String creator = creatorProp == null? "unknown creator" : creatorProp.toString();
      versionNodes.put(current.getURI(), new VersionNode(current.getURI(), versionTitle, creator, i));
      Resource next = current.getPropertyResourceValue(previousProperty);
      current = next;
    }
    sortedVersions = Lists.reverse(sortedVersions);

    System.out.println(sortedVersions.toString());

    Set<RDFNode> seenRevisions = new HashSet<>();
    for (Resource version : sortedVersions) {
      StmtIterator graphRevisionBlankNodes = historyModel.listStatements(version, graphRevisionProperty, (RDFNode) null);

      while (graphRevisionBlankNodes.hasNext()) {
        Resource graphRevisionBlankNode = graphRevisionBlankNodes.nextStatement().getObject().asResource();
        StmtIterator revisionItr = historyModel.listStatements(graphRevisionBlankNode, revisionProperty, (RDFNode) null);
        Resource revisionSubject = revisionItr.next().getObject().asResource();
        StmtIterator stmtIterator1 = historyModel.listStatements(revisionSubject, mergedRevisionProperty, (RDFNode) null);
        if(stmtIterator1.hasNext()) { // it's a merge revision
          Statement mergedRevision = stmtIterator1.next();
          if(!seenRevisions.contains(mergedRevision.getObject())) { // that hasn't been seen before
            seenRevisions.add(mergedRevision.getObject());
            StmtIterator datasetReference = historyModel.listStatements(mergedRevision.getResource(), datasetProperty, (RDFNode) null);
            RDFNode sourceDataset = datasetReference.next().getObject();
            Merge merge = resolveMerge(mergedRevision.getObject().toString(), sourceDataset.toString());
            versionNodes.get(version.getURI()).setMerge(merge);
          }
        }
      }
    }
    System.out.println("basic history nodes");
    System.out.println(versionNodes);
    return new ArrayList<>(versionNodes.values());
  }

  private Merge resolveMerge(String revisionUri,String sourceDatasetUri ) throws IOException, URISyntaxException, RevisionNotFoundException {
    Model history = datasetReadRepository.getHistory(URI.create(sourceDatasetUri));
    Pair<String, String> versionAndGraph = getVersionAndGraph(history, revisionUri);
    String version = versionAndGraph.getLeft();
    String graph = versionAndGraph.getRight();
    String template = IOUtils.toString(new ClassPathResource("getGraphTitle.sparql")
            .getInputStream(), "UTF-8");
    String query = template.replace("$graphUri", graph);
    QueryExecution queryExecution = jenaQueryService.query(sourceDatasetUri + WebConstants.QUERY_ENDPOINT, query);
    ResultSet resultSet = queryExecution.execSelect();
    String title = resultSet.nextSolution().get("title").toString();
    queryExecution.close();

    // find graph and version for the merge revision
    return new Merge(revisionUri, sourceDatasetUri, version, graph, title);
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

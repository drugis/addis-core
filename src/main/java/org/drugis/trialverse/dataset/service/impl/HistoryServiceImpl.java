package org.drugis.trialverse.dataset.service.impl;

import com.google.common.collect.Sets;
import com.jayway.jsonpath.JsonPath;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.ApiKey;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.security.repository.ApiKeyRepository;
import org.drugis.addis.util.WebConstants;
import org.drugis.trialverse.dataset.exception.RevisionNotFoundException;
import org.drugis.trialverse.dataset.model.*;
import org.drugis.trialverse.dataset.repository.DatasetReadRepository;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.dataset.service.HistoryService;
import org.drugis.trialverse.graph.repository.GraphReadRepository;
import org.drugis.trialverse.util.JenaProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

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
  public List<VersionNode> createHistory(URI trialverseDatasetUri) throws IOException, RevisionNotFoundException {
    return createHistory(trialverseDatasetUri, null);
  }

  @Override
  public List<VersionNode> createHistory(URI trialverseDatasetUri, URI trialverseGraphUri) throws IOException, RevisionNotFoundException {
    List<AdvancedVersionNode> advancedHistory = getDatasetHistory(trialverseDatasetUri, trialverseGraphUri);
    return advancedHistory.stream()
            .map(AdvancedVersionNode::simplify)
            .collect(Collectors.toList());
  }

  private List<AdvancedVersionNode> getDatasetHistory(URI trialverseDatasetUri, URI onlyForThisStudy) throws IOException, RevisionNotFoundException {
    List<AdvancedVersionNode> allHistory = getAllHistory(trialverseDatasetUri);
    if (onlyForThisStudy != null) {
      allHistory = filterHistoryForStudy(onlyForThisStudy, allHistory);
    }
    return allHistory;
  }

  private List<AdvancedVersionNode> getAllHistory(URI trialverseDatasetUri) throws IOException, RevisionNotFoundException {
    Model datasetHistoryModel = getHistoryModel(trialverseDatasetUri);
    List<AdvancedVersionNode> sortedDatasetVersionNodes = createSortedVersionList(datasetHistoryModel);

    for (AdvancedVersionNode version : sortedDatasetVersionNodes) {
      buildDatasetVersionHistory(datasetHistoryModel, version);
    }
    return sortedDatasetVersionNodes;
  }

  private void buildDatasetVersionHistory(Model datasetHistoryModel, AdvancedVersionNode version) throws IOException, RevisionNotFoundException {
    Set<RDFNode> seenRevisions = new HashSet<>();
    StmtIterator graphRevisionBlankNodes = datasetHistoryModel.listStatements(datasetHistoryModel.getResource(version.getUri()),
            JenaProperties.graphRevisionProperty, (RDFNode) null);

    while (graphRevisionBlankNodes.hasNext()) {
      Resource revisionSubject = addRevisionAndGetSubject(datasetHistoryModel, version, graphRevisionBlankNodes);
      addMergeIfNeeded(datasetHistoryModel, version, seenRevisions, revisionSubject);
    }
  }

  private void addMergeIfNeeded(Model datasetHistoryModel, AdvancedVersionNode version, Set<RDFNode> seenRevisions, Resource revisionSubject) throws IOException, RevisionNotFoundException {
    StmtIterator mergedRevisionIterator = datasetHistoryModel.listStatements(revisionSubject,
            JenaProperties.mergedRevisionProperty, (RDFNode) null);
    if (mergedRevisionIterator.hasNext()) { // it's a merge revision
      Statement mergedRevision = mergedRevisionIterator.next();
      if (!seenRevisions.contains(mergedRevision.getObject())) { // that hasn't been seen before
        addMergeToDatasetVersion(datasetHistoryModel, seenRevisions, version, mergedRevision);
      }
    }
  }

  private Resource addRevisionAndGetSubject(Model datasetHistoryModel, AdvancedVersionNode version, StmtIterator graphRevisionBlankNodes) {
    Resource graphRevisionBlankNode = graphRevisionBlankNodes.nextStatement().getObject().asResource();
    Resource revisionSubject = getResourceForProperty(datasetHistoryModel, graphRevisionBlankNode, JenaProperties.revisionProperty);
    Resource graphUri = getResourceForProperty(datasetHistoryModel, graphRevisionBlankNode, JenaProperties.graphProperty);
    version.addGraphRevisionPair(URI.create(graphUri.getURI()), URI.create(revisionSubject.getURI()));
    return revisionSubject;
  }

  private Resource getResourceForProperty(Model datasetHistoryModel, Resource graphRevisionBlankNode, Property revisionProperty) {
    StmtIterator revisionIterator = datasetHistoryModel.listStatements(graphRevisionBlankNode,
            revisionProperty, (RDFNode) null);
    return revisionIterator.next().getObject().asResource();
  }

  private void addMergeToDatasetVersion(Model datasetHistoryModel, Set<RDFNode> seenRevisions, AdvancedVersionNode version, Statement mergedRevision) throws IOException, RevisionNotFoundException {
    seenRevisions.add(mergedRevision.getObject());
    StmtIterator datasetReference = datasetHistoryModel.listStatements(mergedRevision.getResource(), JenaProperties.datasetProperty, (RDFNode) null);
    RDFNode sourceDataset = datasetReference.next().getObject();
    Merge merge = resolveMerge(mergedRevision.getObject().toString(), sourceDataset.toString());
    version.setMerge(merge);
  }

  private List<AdvancedVersionNode> filterHistoryForStudy(URI trialverseGraphUri, List<AdvancedVersionNode> sortedDatasetVersionNodes) {
    Map<String, Set<Pair<URI, URI>>> changesByDatasetUri = createChangeSetPerDatasetUri(sortedDatasetVersionNodes);
    sortedDatasetVersionNodes = sortedDatasetVersionNodes.stream()
            .filter(version ->
                    changesByDatasetUri.get(version.getUri()).stream()
                            .anyMatch(graphRevisionPair -> graphRevisionPair.getLeft().equals(trialverseGraphUri)))
            .collect(Collectors.toList());
    return sortedDatasetVersionNodes;
  }

  private Map<String, Set<Pair<URI, URI>>> createChangeSetPerDatasetUri(List<AdvancedVersionNode> sortedDatasetVersionNodes) {
    Map<String, Set<Pair<URI, URI>>> changesByUri = new HashMap<>();

    AdvancedVersionNode previousDatasetVersion = null;
    for (AdvancedVersionNode currentDatasetVersion : sortedDatasetVersionNodes) {
      if (previousDatasetVersion == null) {
        changesByUri.put(currentDatasetVersion.getUri(), currentDatasetVersion.getGraphRevisions());
      } else {
        changesByUri.put(currentDatasetVersion.getUri(), Sets.difference(currentDatasetVersion.getGraphRevisions(), previousDatasetVersion.getGraphRevisions()));
      }
      previousDatasetVersion = currentDatasetVersion;
    }
    return changesByUri;
  }

  private Model getHistoryModel(URI trialverseDatasetUri) throws IOException {
    VersionMapping versionMapping = versionMappingRepository.getVersionMappingByDatasetUrl(trialverseDatasetUri);
    return datasetReadRepository.getHistory(versionMapping.getVersionedDatasetUri());
  }

  private Resource getHeadVersion(Map<String, Resource> versionMap, Map<String, Boolean> referencedMap) {
    // find head version
    Resource headVersion = null;
    for (String key : versionMap.keySet()) {
      if (referencedMap.get(key) == null) {
        headVersion = versionMap.get(key);
      }
    }
    return headVersion;
  }

  @Override
  public VersionNode getVersionInfo(URI trialverseDatasetUri, URI versionUri) throws IOException {
    Model historyModel = getHistoryModel(trialverseDatasetUri);
    Resource versionSubject = historyModel.getResource(versionUri.toString());
    AdvancedVersionNode advancedVersionNode = buildAdvancedNewVersionNode(versionSubject, 0);

    return advancedVersionNode.simplify();
  }

  private List<AdvancedVersionNode> createSortedVersionList(Model historyModel) {
    ResIterator datasetVersionIterator = historyModel.listSubjectsWithProperty(JenaProperties.TYPE_PROPERTY,
            JenaProperties.datasetVersionObject);

    Map<String, Resource> versionMap = new HashMap<>();
    Map<String, Boolean> referencedMap = new HashMap<>();
    populateVersionMaps(datasetVersionIterator, versionMap, referencedMap);

    List<AdvancedVersionNode> sortedVersions = new ArrayList<>();
    Resource current = getHeadVersion(versionMap, referencedMap);
    for (int historyOrder = 0; historyOrder < versionMap.size(); ++historyOrder) {
      sortedVersions.add(buildAdvancedNewVersionNode(current, historyOrder));
      current = current.getPropertyResourceValue(JenaProperties.previousProperty);
    }
    sortedVersions = Lists.reverse(sortedVersions);
    return sortedVersions;
  }

  private AdvancedVersionNode buildAdvancedNewVersionNode(Resource current, int historyOrder) {
    Statement title = current.getProperty(JenaProperties.TITLE_PROPERTY);
    Resource creatorProperty = current.getPropertyResourceValue(JenaProperties.creatorProperty);
    String creator = creatorProperty == null ? "unknown creator" : creatorProperty.toString();
    ApiKey apiKey = null;
    Account account;
    if (creator.equals("unknown creator")) {
      account = null;
    } else {
      if (creator.startsWith(WebConstants.API_KEY_PREFIX)) {
        apiKey = apiKeyRepository.get(Integer.valueOf(creator.substring(WebConstants.API_KEY_PREFIX.length())));
        account = accountRepository.findAccountById(apiKey.getAccountId());
      } else {
        account = accountRepository.findAccountByEmail(creator.substring("mailto:".length()));
      }
      creator = account.getFirstName() + " " + account.getLastName();
    }
    return buildAdvancedVersionNode(current, historyOrder, title, creator, apiKey, account);
  }

  private AdvancedVersionNode buildAdvancedVersionNode(Resource current, int historyOrder, Statement title, String creator, ApiKey apiKey, Account account) {
    Statement descriptionStatement = current.getProperty(JenaProperties.DESCRIPTION_PROPERTY);
    Statement dateProp = current.getProperty(JenaProperties.DATE_PROPERTY);
    return new AdvancedVersionNodeBuilder()
            .setUri(current.getURI())
            .setVersionTitle(title == null ? "" : title.getObject().toString())
            .setVersionDate(((XSDDateTime) dateProp.getObject().asLiteral().getValue()).asCalendar().getTime())
            .setDescription(descriptionStatement == null ? null : descriptionStatement.getObject().toString())
            .setHistoryOrder(historyOrder)
            .setCreator(creator)
            .setUserId(account == null ? null : account.getId())
            .setApplicationName(apiKey == null ? null : apiKey.getApplicationName())
            .build();
  }

  private void populateVersionMaps(ResIterator datasetVersionIterator, Map<String, Resource> versionMap, Map<String, Boolean> referencedMap) {
    while (datasetVersionIterator.hasNext()) {
      Resource resource = datasetVersionIterator.nextResource();
      versionMap.put(resource.getURI(), resource);

      Resource previous = resource.getPropertyResourceValue(JenaProperties.previousProperty);
      if (previous != null) {
        referencedMap.put(previous.getURI(), true);
      }
    }
  }

  private Merge resolveMerge(String revisionUri, String sourceDatasetUri) throws IOException, RevisionNotFoundException {
    VersionMapping mapping = versionMappingRepository.getVersionMappingByVersionedURl(URI.create(sourceDatasetUri));
    Model history = datasetReadRepository.getHistory(URI.create(sourceDatasetUri));
    Pair<String, String> versionAndGraph = getVersionAndGraph(history, revisionUri);
    String userUuid = DigestUtils.sha256Hex(mapping.getOwnerUuid());
    String version = versionAndGraph.getLeft();
    String graph = versionAndGraph.getRight();
    String title = getStudyTitle(mapping, URI.create(version), graph);
    return new Merge(revisionUri, mapping.getDatasetUrl(), version, graph, title, userUuid);
  }

  private String getStudyTitle(VersionMapping mapping, URI version, String graph) throws IOException {
    String template = IOUtils.toString(new ClassPathResource("getGraphTitle.sparql")
            .getInputStream(), "UTF-8");
    String query = template.replace("$graphUri", graph);
    byte[] response = datasetReadRepository.executeQuery(query,
            mapping.getTrialverseDatasetUri(), version, WebConstants.APPLICATION_SPARQL_RESULTS_JSON);
    return JsonPath.read(new String(response), "$.results.bindings[0].title.value");
  }

  private Pair<String, String> getVersionAndGraph(Model historyModel, String revisionUri) throws
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

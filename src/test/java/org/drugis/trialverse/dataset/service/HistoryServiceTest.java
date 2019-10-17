package org.drugis.trialverse.dataset.service;

import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.ApiKey;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.security.repository.ApiKeyRepository;
import org.drugis.addis.util.WebConstants;
import org.drugis.trialverse.dataset.exception.RevisionNotFoundException;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.model.VersionNode;
import org.drugis.trialverse.dataset.repository.DatasetReadRepository;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.dataset.service.impl.HistoryServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class HistoryServiceTest {

  @Mock
  private AccountRepository accountRepository;

  @Mock
  private ApiKeyRepository apiKeyRepository;

  @Mock
  private VersionMappingRepository versionMappingRepository;

  @Mock
  private DatasetReadRepository datasetReadRepository;

  @InjectMocks
  private HistoryService historyService;
  private final URI trialverseDatasetUri = URI.create("http://anyUri");
  private final URI coolGraphUri = URI.create("http://trials.drugis.org/graphs/totallyCoolGraph");
  private final URI uncoolGraphUri = URI.create("http://trials.drugis.org/graphs/uncoolGraph");
  private final URI sourceDatasetHeadVersion = URI.create("http://localhost:8080/versions/sourceHeadVersion");
  private final String versionedDatasetUri = "http://anyVersionedUri";
  private final VersionMapping mapping = new VersionMapping(versionedDatasetUri, null, trialverseDatasetUri.toString());
  private final InputStream historyStream = new ClassPathResource("mockHistory.ttl").getInputStream();
  private final Model historyModel = ModelFactory.createDefaultModel();
  private final Integer apiKeyId = 1;
  private final ApiKey apiKey = mock(ApiKey.class);
  private final String email = "flutadres@gmail.com";
  private final Account account = new Account("userName", "firstName", "lastName", email);
  private URI sourceDatasetUri = URI.create("http://localhost:8080/datasets/sourceDataset");
  private String versionedSourceDatasetUri = "http://versionedSourceDatasetUri";
  private final VersionMapping sourceMapping = new VersionMapping(versionedSourceDatasetUri, "sourceOwnerId", sourceDatasetUri.toString());
  private final Model sourceHistoryModel = ModelFactory.createDefaultModel();
  private final InputStream sourceHistoryStream = new ClassPathResource("mockMergeSourceHistory.ttl").getInputStream();
  private final byte[] response = ("{\"results\": { \"bindings\": [ { \"title\": { \"value\": \"sourceTitle\"}}]}}").getBytes();

  public HistoryServiceTest() throws IOException {
  }

  @Before
  public void setUp() throws IOException {
    historyService = new HistoryServiceImpl();
    initMocks(this);

    historyModel.read(historyStream, null, "TTL");
    sourceHistoryModel.read(sourceHistoryStream, null, "TTL");
    when(versionMappingRepository.getVersionMappingByDatasetUrl(trialverseDatasetUri)).thenReturn(mapping);
    when(datasetReadRepository.getHistory(mapping.getVersionedDatasetUri())).thenReturn(historyModel);
    when(apiKeyRepository.get(apiKeyId)).thenReturn(apiKey);
    when(accountRepository.findAccountByEmail(email)).thenReturn(account);

    when(versionMappingRepository.getVersionMappingByVersionedURl(sourceDatasetUri)).thenReturn(sourceMapping);
    when(datasetReadRepository.getHistory(sourceDatasetUri)).thenReturn(sourceHistoryModel);
    String query = createQuery();
    when(datasetReadRepository.executeQuery(query,
            sourceMapping.getTrialverseDatasetUri(),
            sourceDatasetHeadVersion,
            WebConstants.APPLICATION_SPARQL_RESULTS_JSON)).thenReturn(response);
  }

  private String createQuery() throws IOException {
    String template = IOUtils.toString(new ClassPathResource("getGraphTitle.sparql")
            .getInputStream(), "UTF-8");
    return template.replace("$graphUri", "http://trials.drugis.org/graphs/sourceGraph");
  }

  @Test
  public void testCreateHistory() throws RevisionNotFoundException, IOException, URISyntaxException {
    List<VersionNode> history = historyService.createHistory(trialverseDatasetUri);

    assertEquals(4, history.size());
    VersionNode versionNode = history.get(3);
    assertEquals("http://localhost:8080/versions/headVersion", versionNode.getUri());
    assertEquals("Edit both graphs somehow", versionNode.getVersionTitle());
    assertNull(versionNode.getDescription());
    assertEquals(new Date(1478613838000L), history.get(1).getVersionDate());

  }

  @Test
  public void testCreateFilteredHistory() throws RevisionNotFoundException, IOException, URISyntaxException {
    List<VersionNode> coolHistory = historyService.createHistory(trialverseDatasetUri, coolGraphUri);

    assertEquals(3, coolHistory.size());
    VersionNode coolCreationVersion = coolHistory.get(0);
    assertEquals("Create both graphs", coolCreationVersion.getVersionTitle());

    List<VersionNode> uncoolHistory = historyService.createHistory(trialverseDatasetUri, uncoolGraphUri);
    assertEquals(1, uncoolHistory.size());
    VersionNode uncoolCreationVersion = uncoolHistory.get(0);
    assertEquals("Create both graphs", uncoolCreationVersion.getVersionTitle());
  }

  @Test
  public void testGetSingleHistoryInfo() throws IOException, URISyntaxException {
    VersionNode versionInfo = historyService.getVersionInfo(trialverseDatasetUri, URI.create("http://localhost:8080/versions/twoBeforeHeadVersion"));

    assertNotNull(versionInfo);
    assertEquals("Create both graphs", versionInfo.getVersionTitle());
  }

}
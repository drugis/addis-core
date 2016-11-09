package org.drugis.trialverse.dataset.service;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.drugis.trialverse.dataset.exception.RevisionNotFoundException;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.model.VersionNode;
import org.drugis.trialverse.dataset.repository.DatasetReadRepository;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.dataset.service.impl.HistoryServiceImpl;
import org.drugis.trialverse.graph.repository.GraphReadRepository;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.ApiKey;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.security.repository.ApiKeyRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by daan on 3-9-15.
 */
public class HistoryServiceTest {

  @Mock
  private AccountRepository accountRepository;

  @Mock
  private ApiKeyRepository apiKeyRepository;

  @Mock
  private VersionMappingRepository versionMappingRepository;

  @Mock
  private GraphReadRepository graphReadRepository;

  @Mock
  private DatasetReadRepository datasetReadRepository;

  @Mock
  private RestTemplate restTemplate;

  @InjectMocks
  private HistoryService historyService;
  private final URI trialverseDatasetUri = URI.create("http://anyUri");
  private final URI trialverseGraphUri = URI.create("http://trials.drugis.org/graphs/totallyCoolGraph");
  private final String versionedDatasetUri = "http://anyVersionedUri";
  private final VersionMapping mapping = new VersionMapping(versionedDatasetUri, null, trialverseDatasetUri.toString());
  private final InputStream historyStream = new ClassPathResource("mockHistory.ttl").getInputStream();
  private final Model historyModel = ModelFactory.createDefaultModel();
  private final Integer apiKeyId = 1;
  private final ApiKey apiKey = mock(ApiKey.class);
  private final String email = "flutadres@gmail.com";
  private final Account account = new Account("userName", "firstName", "lastName", email);

  public HistoryServiceTest() throws IOException {
  }

  @Before
  public void setUp() throws URISyntaxException, IOException {
    historyService = new HistoryServiceImpl();
    initMocks(this);
    historyModel.read(historyStream, null, "TTL");
    when(versionMappingRepository.getVersionMappingByDatasetUrl(trialverseDatasetUri)).thenReturn(mapping);
    when(datasetReadRepository.getHistory(mapping.getVersionedDatasetUri())).thenReturn(historyModel);
    when(apiKeyRepository.get(apiKeyId)).thenReturn(apiKey);
    when(accountRepository.findAccountByEmail(email)).thenReturn(account);
  }

  @Test
  public void testCreateHistory() throws RevisionNotFoundException, IOException, URISyntaxException {

    List<VersionNode> history = historyService.createHistory(trialverseDatasetUri);

    assertEquals(5, history.size());
    VersionNode versionNode = history.get(4);
    assertEquals("http://localhost:8080/versions/headVersion", versionNode.getUri());
    assertEquals("add arm to group 2", versionNode.getVersionTitle());
    assertNull(versionNode.getDescription());
    assertEquals(new Date(1478613838000L), history.get(1).getVersionDate());

  }

  @Test
  public void testCreateFilteredHistory() throws RevisionNotFoundException, IOException, URISyntaxException {

    List<VersionNode> history = historyService.createHistory(trialverseDatasetUri, trialverseGraphUri);

    assertEquals(2, history.size());
    VersionNode initialCreationVersion = history.get(0);
    assertEquals("Initial study creation: graph 1", initialCreationVersion.getVersionTitle());
  }

}
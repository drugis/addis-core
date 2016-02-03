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

  @Before
  public void setUp() {
    historyService = new HistoryServiceImpl();
    initMocks(this);
  }

  @Test
  public void testCreateHistory() throws RevisionNotFoundException, IOException, URISyntaxException {
    URI trialverseDatasetUri = URI.create("http://anyUri");
    String versionedDatasetUri = "http://anyVersionedUri";
    VersionMapping mapping = new VersionMapping(versionedDatasetUri, null, trialverseDatasetUri.toString());
    InputStream historyStream = new ClassPathResource("mockHistory.ttl").getInputStream();
    Model historyModel = ModelFactory.createDefaultModel();
    historyModel.read(historyStream, null, "TTL");

    when(versionMappingRepository.getVersionMappingByDatasetUrl(trialverseDatasetUri)).thenReturn(mapping);
    when(datasetReadRepository.getHistory(mapping.getVersionedDatasetUri())).thenReturn(historyModel);
    Integer apiKeyId = 1;
    ApiKey apiKey = mock(ApiKey.class);
    when(apiKeyRepository.get(apiKeyId)).thenReturn(apiKey);
    String email = "osmosisch@gmail.com";
    Account account = new Account("userName", "firstName", "lastName", email);
    when(accountRepository.findAccountByEmail(email)).thenReturn(account);
    List<VersionNode> history = historyService.createHistory(trialverseDatasetUri);

    assertTrue(history.size() > 0);
    VersionNode versionNode = history.get(2);
    assertEquals("http://testhost/versions/e53caa0d-c0df-46db-977e-37f48fecb042", versionNode.getUri());
    assertEquals("Added an arm", versionNode.getVersionTitle());
    assertEquals("because I could", versionNode.getDescription());
    assertEquals(null, history.get(1).getDescription());
    assertEquals(new Date(1440672031000L), history.get(1).getVersionDate());
  }

}
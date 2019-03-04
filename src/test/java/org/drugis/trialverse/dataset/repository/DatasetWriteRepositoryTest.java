package org.drugis.trialverse.dataset.repository;

import org.drugis.addis.security.Account;
import org.drugis.addis.security.ApiKey;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.util.WebConstants;
import org.drugis.trialverse.dataset.exception.EditDatasetException;
import org.drugis.trialverse.dataset.factory.JenaFactory;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.repository.impl.DatasetWriteRepositoryImpl;
import org.drugis.trialverse.security.TrialversePrincipal;
import org.drugis.trialverse.util.Namespaces;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class DatasetWriteRepositoryTest {

  private static final java.lang.String DATASET_URI = "http://mockserver";

  @Mock
  private VersionMappingRepository versionMappingRepository;

  @Mock
  private JenaFactory jenaFactory;

  @Mock
  private RestTemplate restTemplate;

  @Mock
  private AccountRepository accountRepository;

  @Mock
  private WebConstants webconstants;

  @InjectMocks
  DatasetWriteRepository datasetWriteRepository;

  private ApiKey apiKey = mock(ApiKey.class);
  Account account = new Account("username", "john", "doe", "foo@bar.com");
  Principal principalMock = new PreAuthenticatedAuthenticationToken(account, apiKey);
  TrialversePrincipal owner = new TrialversePrincipal(principalMock);


  @Before
  public void setUp() {
    datasetWriteRepository = new DatasetWriteRepositoryImpl();
    MockitoAnnotations.initMocks(this);
    when(webconstants.getTriplestoreBaseUri()).thenReturn("http://jena-es:8080");
    when(jenaFactory.createDatasetURI()).thenReturn(DATASET_URI + "/someMockUuid");
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(accountRepository);
  }

  @Test
  public void testCreateDataset() throws Exception {
    String title = "my-title";
    String description = "description";
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add("Location", "http://location");
    ResponseEntity responseEntity = new ResponseEntity(httpHeaders, HttpStatus.CREATED);
    when(restTemplate.postForEntity(anyString(), anyObject(), any(Class.class))).thenReturn(responseEntity);
    when(accountRepository.findAccountByUsername(owner.getUserName())).thenReturn(account);

    URI result = datasetWriteRepository.createDataset(title, description, owner);

    assertTrue(result.toString().startsWith(DATASET_URI + "/someMockUuid"));
    verify(accountRepository, times(2)).findAccountByUsername(owner.getUserName());
  }

  @Test
  public void testCreateDatasetWithNullDescription() throws Exception {
    String title = "my-title";
    String description = null;
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add("Location", "http://location");
    ResponseEntity responseEntity = new ResponseEntity(httpHeaders, HttpStatus.CREATED);
    when(restTemplate.postForEntity(anyString(), anyObject(), any(Class.class))).thenReturn(responseEntity);
    when(accountRepository.findAccountByUsername(owner.getUserName())).thenReturn(account);

    URI result = datasetWriteRepository.createDataset(title, description, owner);

    assertTrue(result.toString().startsWith(DATASET_URI + "/someMockUuid"));
    verify(accountRepository, times(2)).findAccountByUsername(owner.getUserName());
  }

  @Test
  public void testEditDataset() throws URISyntaxException, EditDatasetException {
    String datasetUuid = "datasetUuid";
    URI datasetUri = URI.create(Namespaces.DATASET_NAMESPACE + datasetUuid);
    String newTitle = "new title";

    HttpHeaders headers = new HttpHeaders();
    headers.add(WebConstants.X_EVENT_SOURCE_VERSION, "newVersion");
    ResponseEntity responseEntity = new ResponseEntity(headers, HttpStatus.OK);

    VersionMapping mapping = new VersionMapping("versionedUrl", account.getEmail(), datasetUri.toString());

    when(versionMappingRepository.getVersionMappingByDatasetUrl(datasetUri)).thenReturn(mapping);
    when(restTemplate.postForEntity(anyString(), anyObject(), any(Class.class))).thenReturn(responseEntity);

    String newVersion = datasetWriteRepository.editDataset(owner, mapping, newTitle, null);

    assertEquals("newVersion", newVersion);
    verify(accountRepository).findAccountByUsername(owner.getUserName());
  }


}
package org.drugis.trialverse.dataset.repository;

import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.trialverse.dataset.factory.JenaFactory;
import org.drugis.trialverse.dataset.repository.impl.DatasetWriteRepositoryImpl;
import org.drugis.addis.security.ApiKey;
import org.drugis.trialverse.security.TrialversePrincipal;
import org.drugis.trialverse.util.WebConstants;
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
import java.security.Principal;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class DatasetWriteRepositoryTest {

  private static final java.lang.String DATASET_URI = "http://mockserver";

  @Mock
  private WebConstants webConstants;

  @Mock
  private VersionMappingRepository versionMappingRepository;

  @Mock
  private JenaFactory jenaFactory;

  @Mock
  private RestTemplate restTemplate;

  @Mock
  private AccountRepository accountRepository;

  @InjectMocks
  DatasetWriteRepository datasetWriteRepository;



  @Before
  public void setUp() {
    datasetWriteRepository = new DatasetWriteRepositoryImpl();

    MockitoAnnotations.initMocks(this);

    when(webConstants.getTriplestoreBaseUri()).thenReturn(DATASET_URI);
    when(jenaFactory.createDatasetURI()).thenReturn(DATASET_URI + "/someMockUuid");

  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(webConstants, accountRepository);
  }

  @Test
  public void testCreateDataset() throws Exception {
    ApiKey apiKey = mock(ApiKey.class);
    Account account = new Account("username", "john", "doe", "foo@bar.com");
    Principal principalMock = new PreAuthenticatedAuthenticationToken(account, apiKey);
    TrialversePrincipal owner = new TrialversePrincipal(principalMock);
    String title = "my-title";
    String description = "description";
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add("Location", "http://location");
    ResponseEntity responseEntity = new ResponseEntity(httpHeaders, HttpStatus.CREATED);
    when(restTemplate.postForEntity(anyString(), anyObject(), any(Class.class))).thenReturn(responseEntity);
    when(accountRepository.findAccountByUsername(owner.getUserName())).thenReturn(account);

    URI result = datasetWriteRepository.createDataset(title, description, owner);

    assertTrue(result.toString().startsWith(DATASET_URI + "/someMockUuid"));
    verify(webConstants).getTriplestoreBaseUri();
    verify(accountRepository).findAccountByUsername(owner.getUserName());
  }

  @Test
  public void testCreateDatasetWithNullDescription() throws Exception {
    Account account = new Account("username", "john", "doe", "foo@bar.com");
    ApiKey apiKey = mock(ApiKey.class);
    Principal principalMock = new PreAuthenticatedAuthenticationToken(account, apiKey);
    TrialversePrincipal owner = new TrialversePrincipal(principalMock);
    String title = "my-title";
    String description = null;
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add("Location", "http://location");
    ResponseEntity responseEntity = new ResponseEntity(httpHeaders, HttpStatus.CREATED);
    when(restTemplate.postForEntity(anyString(), anyObject(), any(Class.class))).thenReturn(responseEntity);
    when(accountRepository.findAccountByUsername(owner.getUserName())).thenReturn(account);

    URI result = datasetWriteRepository.createDataset(title, description, owner);

    assertTrue(result.toString().startsWith(DATASET_URI + "/someMockUuid"));
    verify(webConstants).getTriplestoreBaseUri();
    verify(accountRepository).findAccountByUsername(owner.getUserName());
  }
}
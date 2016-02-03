package org.drugis.trialverse.graph.repository;

import org.apache.commons.io.IOUtils;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicStatusLine;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.trialverse.dataset.factory.HttpClientFactory;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.graph.exception.UpdateGraphException;
import org.drugis.trialverse.graph.repository.impl.GraphWriteRepositoryImpl;
import org.drugis.addis.security.ApiKey;
import org.drugis.addis.security.AuthenticationService;
import org.drugis.trialverse.security.TrialversePrincipal;
import org.drugis.trialverse.util.Namespaces;
import org.drugis.trialverse.util.WebConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mock.web.DelegatingServletInputStream;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class GraphWriteRepositoryTest {

  HttpClient mockHttpClient = mock(HttpClient.class);
  HttpResponse mockResponse = mock(HttpResponse.class);
  BasicHeader versionHeader = new BasicHeader(WebConstants.X_EVENT_SOURCE_VERSION, "version 37");

  @Mock
  private HttpClientFactory httpClientFactory;

  @Mock
  private WebConstants webConstants;

  @Mock
  private VersionMappingRepository versionMappingRepository;

  @Mock
  private HttpClient httpClient;

  @Mock
  private AuthenticationService authenticationService;

  @Mock
  private AccountRepository accountRepository;

  @InjectMocks
  GraphWriteRepository graphWriteRepository;

  private String email = "foo@bar.com";
  private String username = "username";
  private Account account = new Account(username, "firstname", "lastName", email);

  @Before
  public void setUp() throws IOException {
    webConstants = mock(WebConstants.class);
    graphWriteRepository = new GraphWriteRepositoryImpl();
    initMocks(this);
    reset(httpClientFactory, mockHttpClient);
    when(webConstants.getTriplestoreDataUri()).thenReturn("BaseUri/current");
    when(httpClientFactory.build()).thenReturn(mockHttpClient);
    PreAuthenticatedAuthenticationToken principal = new PreAuthenticatedAuthenticationToken(account, new ApiKey());
    TrialversePrincipal authentication = new TrialversePrincipal(principal);
    when(authenticationService.getAuthentication()).thenReturn(authentication);
    mockResponse = mock(CloseableHttpResponse.class);
    HttpEntity entity = mock(HttpEntity.class);
    when(mockResponse.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "FINE!"));
    when(entity.getContent()).thenReturn(getClass().getClassLoader().getResourceAsStream("result.txt"));
    when(mockResponse.getEntity()).thenReturn(entity);
    when(mockResponse.getFirstHeader(WebConstants.X_EVENT_SOURCE_VERSION)).thenReturn(versionHeader);
    when(httpClient.execute(any(HttpPut.class))).thenReturn(mockResponse);
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(mockHttpClient);
    verifyNoMoreInteractions(versionMappingRepository);
    verifyNoMoreInteractions(httpClient);
  }

  @Test
  public void testUpdateGraphWithoutDescription() throws IOException, URISyntaxException, UpdateGraphException {
    String datasetUuid = "datasetuuid";
    String graphUuid = "graphUuid";
    HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
    InputStream inputStream = IOUtils.toInputStream("content");
    DelegatingServletInputStream delegatingServletInputStream = new DelegatingServletInputStream(inputStream);
    when(mockHttpServletRequest.getInputStream()).thenReturn(delegatingServletInputStream);
    String titleValue = "test title header";
    when(mockHttpServletRequest.getParameter(WebConstants.COMMIT_TITLE_PARAM)).thenReturn(titleValue);
    URI datasetUrl = new URI(Namespaces.DATASET_NAMESPACE + datasetUuid);
    String versionStoreDatasetUri = "http://versionstoreUri";
    VersionMapping versionMapping = new VersionMapping(1, versionStoreDatasetUri, "userName", datasetUrl.toString());

    when(versionMappingRepository.getVersionMappingByDatasetUrl(datasetUrl)).thenReturn(versionMapping);
    when(accountRepository.findAccountByUsername(username)).thenReturn(account);

    Header resultHeader = graphWriteRepository.updateGraph(datasetUrl, graphUuid, delegatingServletInputStream, titleValue, null );

    assertEquals(versionHeader, resultHeader);

    verify(httpClient).execute(any(HttpPut.class));
    verify(versionMappingRepository).getVersionMappingByDatasetUrl(datasetUrl);
    verify(accountRepository).findAccountByUsername(username);
  }

  @Test
  public void testUpdateGraphWithDescription() throws IOException, URISyntaxException, UpdateGraphException {
    String datasetUuid = "datasetuuid";
    String graphUuid = "graphUuid";
    HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
    InputStream inputStream = IOUtils.toInputStream("content");
    DelegatingServletInputStream delegatingServletInputStream = new DelegatingServletInputStream(inputStream);
    when(mockHttpServletRequest.getInputStream()).thenReturn(delegatingServletInputStream);
    String titleValue = "test title header";
    when(mockHttpServletRequest.getParameter(WebConstants.COMMIT_TITLE_PARAM)).thenReturn(titleValue);
    String descriptionValue = "test description";
    when(mockHttpServletRequest.getParameter(WebConstants.COMMIT_DESCRIPTION_PARAM)).thenReturn(descriptionValue);
    URI datasetUrl = new URI(Namespaces.DATASET_NAMESPACE + datasetUuid);
    String versionStoreDatasetUri = "http://versionStoreDatasetUri";
    VersionMapping versionMapping = new VersionMapping(1, versionStoreDatasetUri, "userName", datasetUrl.toString());
    when(versionMappingRepository.getVersionMappingByDatasetUrl(datasetUrl)).thenReturn(versionMapping);

    Header resultHeader = graphWriteRepository.updateGraph(datasetUrl, graphUuid, delegatingServletInputStream, titleValue, descriptionValue);

    assertEquals(versionHeader, resultHeader);

    verify(httpClient).execute(any(HttpPut.class));
    verify(versionMappingRepository).getVersionMappingByDatasetUrl(datasetUrl);
  }

}
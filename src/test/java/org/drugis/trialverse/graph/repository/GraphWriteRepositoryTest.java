package org.drugis.trialverse.graph.repository;

import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicStatusLine;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.ApiKey;
import org.drugis.addis.security.AuthenticationService;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.util.WebConstants;
import org.drugis.trialverse.dataset.factory.HttpClientFactory;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.graph.exception.DeleteGraphException;
import org.drugis.trialverse.graph.exception.UpdateGraphException;
import org.drugis.trialverse.graph.repository.impl.GraphWriteRepositoryImpl;
import org.drugis.trialverse.graph.service.GraphService;
import org.drugis.trialverse.security.TrialversePrincipal;
import org.drugis.trialverse.util.Namespaces;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.DelegatingServletInputStream;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.mockito.MockitoAnnotations.openMocks;

public class GraphWriteRepositoryTest {

  private HttpClient mockHttpClient = mock(HttpClient.class);
  private BasicHeader versionHeader = new BasicHeader(WebConstants.X_EVENT_SOURCE_VERSION, "version 37");

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

  @Mock
  private GraphService graphService;

  @InjectMocks
  GraphWriteRepository graphWriteRepository;

  private String email = "foo@bar.com";
  private String username = "username";
  private Account account = new Account(username, "firstname", "lastName", email);
  AutoCloseable closeable;
  @Before
  public void setUp() throws IOException {
    webConstants = mock(WebConstants.class);
    graphWriteRepository = new GraphWriteRepositoryImpl();
    closeable = openMocks(this);
    reset(httpClientFactory, mockHttpClient);
    when(webConstants.getTriplestoreDataUri()).thenReturn("BaseUri/current");
    when(httpClientFactory.build()).thenReturn(mockHttpClient);
    PreAuthenticatedAuthenticationToken principal = new PreAuthenticatedAuthenticationToken(account, new ApiKey());
    TrialversePrincipal authentication = new TrialversePrincipal(principal);
    when(authenticationService.getAuthentication()).thenReturn(authentication);
    HttpResponse mockResponse = mock(CloseableHttpResponse.class);
    HttpEntity entity = mock(HttpEntity.class);
    when(mockResponse.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "FINE!"));
    when(entity.getContent()).thenReturn(getClass().getClassLoader().getResourceAsStream("result.txt"));
    when(mockResponse.getEntity()).thenReturn(entity);
    when(mockResponse.getFirstHeader(WebConstants.X_EVENT_SOURCE_VERSION)).thenReturn(versionHeader);
    when(httpClient.execute(any(HttpRequestBase.class))).thenReturn(mockResponse);
  }

  @After
  public void tearDown() throws Exception {
    verifyNoMoreInteractions(mockHttpClient);
    verifyNoMoreInteractions(versionMappingRepository);
    verifyNoMoreInteractions(httpClient);
    closeable.close();
  }

  @Test
  public void testUpdateGraphWithoutDescription() throws IOException, URISyntaxException, UpdateGraphException {
    String datasetUuid = "datasetuuid";
    String graphUuid = "graphUuid";
    HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
    InputStream inputStream = new ByteArrayInputStream("content".getBytes(UTF_8));
    DelegatingServletInputStream delegatingServletInputStream = new DelegatingServletInputStream(inputStream);
    when(mockHttpServletRequest.getInputStream()).thenReturn(delegatingServletInputStream);
    String titleValue = "test title header";
    when(mockHttpServletRequest.getParameter(WebConstants.COMMIT_TITLE_PARAM)).thenReturn(titleValue);
    URI datasetUrl = new URI(Namespaces.DATASET_NAMESPACE + datasetUuid);

    when(accountRepository.findAccountByUsername(username)).thenReturn(account);
    when(graphService.buildGraphUri(graphUuid)).thenReturn(URI.create(Namespaces.GRAPH_NAMESPACE + graphUuid));
    Header resultHeader = graphWriteRepository.updateGraph(datasetUrl, graphUuid, delegatingServletInputStream, titleValue, null );

    assertEquals(versionHeader, resultHeader);

    verify(httpClient).execute(any(HttpPut.class));
    verify(accountRepository).findAccountByUsername(username);
  }

  @Test
  public void testUpdateGraphWithDescription() throws IOException, URISyntaxException, UpdateGraphException {
    String datasetUuid = "datasetuuid";
    String graphUuid = "graphUuid";
    HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
    InputStream inputStream = new ByteArrayInputStream("content".getBytes(UTF_8));
    DelegatingServletInputStream delegatingServletInputStream = new DelegatingServletInputStream(inputStream);
    when(mockHttpServletRequest.getInputStream()).thenReturn(delegatingServletInputStream);
    String titleValue = "test title header";
    when(mockHttpServletRequest.getParameter(WebConstants.COMMIT_TITLE_PARAM)).thenReturn(titleValue);
    String descriptionValue = "test description";
    when(mockHttpServletRequest.getParameter(WebConstants.COMMIT_DESCRIPTION_PARAM)).thenReturn(descriptionValue);
    URI datasetUrl = new URI(Namespaces.DATASET_NAMESPACE + datasetUuid);

    Header resultHeader = graphWriteRepository.updateGraph(datasetUrl, graphUuid, delegatingServletInputStream, titleValue, descriptionValue);

    assertEquals(versionHeader, resultHeader);

    verify(httpClient).execute(any(HttpPut.class));
  }

  @Test
  public void testDeleteGraph() throws DeleteGraphException, IOException, URISyntaxException {
    String datasetUuid = "datasetuuid";
    String graphUuid = "graphUuid";
    URI datasetUrl = new URI(Namespaces.DATASET_NAMESPACE + datasetUuid);

    Header resultHeader = graphWriteRepository.deleteGraph(datasetUrl, graphUuid);

    assertEquals(versionHeader, resultHeader);
    verify(httpClient).execute(any(HttpDelete.class));
  }

}
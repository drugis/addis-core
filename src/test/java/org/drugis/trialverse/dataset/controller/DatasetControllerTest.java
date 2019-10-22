package org.drugis.trialverse.dataset.controller;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.WebContent;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.util.WebConstants;
import org.drugis.trialverse.dataset.controller.command.DatasetController;
import org.drugis.trialverse.dataset.controller.command.DatasetCommand;
import org.drugis.trialverse.dataset.model.Dataset;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.model.VersionNode;
import org.drugis.trialverse.dataset.repository.DatasetReadRepository;
import org.drugis.trialverse.dataset.repository.DatasetWriteRepository;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.dataset.service.DatasetService;
import org.drugis.trialverse.dataset.service.HistoryService;
import org.drugis.trialverse.graph.service.GraphService;
import org.drugis.trialverse.security.TrialversePrincipal;
import org.drugis.trialverse.util.Namespaces;
import org.drugis.trialverse.util.Utils;
import org.drugis.trialverse.util.service.TrialverseIOUtilsService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionData;
import org.springframework.social.security.SocialAuthenticationToken;
import org.springframework.social.security.SocialUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by connor on 6-11-14.
 */
@Configuration
@EnableWebMvc
public class DatasetControllerTest {

  private MockMvc mockMvc;

  @Mock
  private AccountRepository accountRepository;

  @Mock
  private DatasetWriteRepository datasetWriteRepository;

  @Mock
  private DatasetReadRepository datasetReadRepository;

  @Mock
  private TrialverseIOUtilsService trialverseIOUtilsService;

  @Mock
  private WebConstants webConstants;

  @Mock
  private HistoryService historyService;

  @Mock
  private DatasetService datasetService;

  @Mock
  private VersionMappingRepository versionMappingRepository;

  @InjectMocks
  private DatasetController datasetController;

  private TrialversePrincipal principal;
  private SocialAuthenticationToken user;
  private Account john = new Account(1, "john@apple.co.uk", "John", "Lennon", DigestUtils.sha256Hex("john@apple.co.uk"));
  private String datasetUuid = "someDataset";

  @Before
  public void setUp() {
    Connection connection = mock(Connection.class);
    ConnectionData connectionData = mock(ConnectionData.class);
    when(connectionData.getProviderId()).thenReturn("providerId");
    when(connection.createData()).thenReturn(connectionData);

    user = new SocialAuthenticationToken(connection, new SocialUser(john.getUsername(), "password", Collections.emptyList()), null, null);
    principal = new TrialversePrincipal(user);
    accountRepository = mock(AccountRepository.class);
    datasetWriteRepository = mock(DatasetWriteRepository.class);
    versionMappingRepository = mock(VersionMappingRepository.class);
    datasetController = new DatasetController();
    webConstants = mock(WebConstants.class);

    initMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(datasetController).build();
    when(user.getName()).thenReturn(john.getUsername());
  }


  @After
  public void tearDown() {
    verifyNoMoreInteractions(
            accountRepository,
            datasetWriteRepository,
            datasetService,
            versionMappingRepository
    );
  }

  @Test
  public void testCreateDataset() throws Exception {
    String newDatasetUri = "http://some.thing.like/this/asd123";
    URI uri = new URI(newDatasetUri);
    DatasetCommand datasetCommand = new DatasetCommand("dataset title");
    String jsonContent = Utils.createJson(datasetCommand);
    when(datasetWriteRepository.createDataset(datasetCommand.getTitle(), datasetCommand.getDescription(), principal)).thenReturn(uri);
    Integer userId = john.getId();
    mockMvc
            .perform(post("/users/" + userId + "/datasets")
                    .principal(user)
                    .content(jsonContent)
                    .contentType(webConstants.getApplicationJsonUtf8())
            )
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", newDatasetUri));

    verify(datasetWriteRepository).createDataset(datasetCommand.getTitle(), datasetCommand.getDescription(), principal);
    verify(datasetService).checkDatasetOwner(1, user);
  }

  @Test
  public void queryDatasetsRequestPathTurtleType() throws Exception {
    Model model = mock(Model.class);
    when(datasetReadRepository.queryDatasets(john)).thenReturn(model);
    Integer userId = 1;
    when(accountRepository.findAccountById(userId)).thenReturn(john);

    mockMvc.perform(get("/users/" + userId + "/datasets").principal(user)
            .accept(RDFLanguages.TURTLE.getHeaderString()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(RDFLanguages.TURTLE.getContentType().getContentType()));

    verify(accountRepository).findAccountById(userId);
    verify(datasetReadRepository).queryDatasets(john);
    verify(trialverseIOUtilsService).writeModelToServletResponse(Matchers.any(Model.class), Matchers.any(HttpServletResponse.class));
  }

  @Test
  public void queryDatasetsRequestPathJsonType() throws Exception {
    String archivedOn = new Date().toString();
    List<Dataset> datasets = Arrays.asList(new Dataset("uri", john, "notArchived", "description", "headVersion", false, archivedOn),
            new Dataset("uri", john, "archived", "archived description", "headVersion", true, archivedOn));
    when(datasetService.findDatasets(john)).thenReturn(datasets);
    Integer userId = 1;
    when(accountRepository.findAccountById(userId)).thenReturn(john);

    mockMvc.perform(get("/users/" + userId + "/datasets").principal(user)
            .accept(WebConstants.getApplicationJsonUtf8()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.getApplicationJsonUtf8()))
            .andExpect(jsonPath("$", hasSize(datasets.size())));

    verify(accountRepository).findAccountById(userId);
    verify(datasetService).findDatasets(john);
  }

  @Test
  public void queryDatasetsGraphs() {
    Model model = mock(Model.class);
    HttpServletResponse mockServletResponse = mock(HttpServletResponse.class);
    when(datasetReadRepository.queryDatasets(john)).thenReturn(model);
    Integer userId = 1;
    when(accountRepository.findAccountById(userId)).thenReturn(john);

    datasetController.queryDatasetsGraphsByUser(mockServletResponse, userId);

    verify(accountRepository).findAccountById(userId);
    verify(datasetReadRepository).queryDatasets(john);
    verify(trialverseIOUtilsService).writeModelToServletResponse(model, mockServletResponse);
  }

  @Test
  public void testGetDataset() throws Exception {
    URI datasetUri = new URI(Namespaces.DATASET_NAMESPACE + datasetUuid);
    Model model = mock(Model.class);
    when(datasetReadRepository.getVersionedDataset(datasetUri, null)).thenReturn(model);
    when(accountRepository.findAccountByUsername(user.getName())).thenReturn(john);

    mockMvc.perform((get("/users/some-user-uid/datasets/" + datasetUuid)).principal(user))
            .andExpect(status().isOk())
            .andExpect(content().contentType(RDFLanguages.TURTLE.getContentType().getContentType()));

    verify(datasetReadRepository).getVersionedDataset(datasetUri, null);
    verify(trialverseIOUtilsService).writeModelToServletResponse(Matchers.any(Model.class), Matchers.any(HttpServletResponse.class));
  }

  @Test
  public void testGetDatasetAsJson() throws Exception {
    URI datasetUri = new URI(Namespaces.DATASET_NAMESPACE + datasetUuid);
    Model model = mock(Model.class);
    when(datasetReadRepository.getVersionedDataset(datasetUri, null)).thenReturn(model);
    when(accountRepository.findAccountByUsername(user.getName())).thenReturn(john);

    mockMvc.perform((get("/users/some-user-uid/datasets/" + datasetUuid)).principal(user).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(RDFLanguages.JSONLD.getContentType().getContentType()));

    verify(datasetReadRepository).getVersionedDataset(datasetUri, null);
    verify(trialverseIOUtilsService).writeModelToServletResponseJson(Matchers.any(Model.class), Matchers.any(HttpServletResponse.class));
  }

  @Test
  public void testGetDatasetVersion() throws Exception {
    String versionUuid = "versionUuid";

    URI datasetUri = new URI(Namespaces.DATASET_NAMESPACE + datasetUuid);
    Model model = mock(Model.class);
    when(datasetReadRepository.getVersionedDataset(datasetUri, versionUuid)).thenReturn(model);
    when(accountRepository.findAccountByUsername(user.getName())).thenReturn(john);

    mockMvc.perform((get("/users/user-name-hash/datasets/" + datasetUuid + "/versions/" + versionUuid)).principal(user))
            .andExpect(status().isOk())
            .andExpect(content().contentType(RDFLanguages.TURTLE.getContentType().getContentType()));

    verify(datasetReadRepository).getVersionedDataset(datasetUri, versionUuid);
    verify(trialverseIOUtilsService).writeModelToServletResponse(Matchers.any(Model.class), Matchers.any(HttpServletResponse.class));
  }

  @Test
  public void testGetHistory() throws Exception {
    mockMvc.perform((get("/users/user-name-hash/datasets/" + datasetUuid + "/history")).principal(user))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    verify(historyService).createHistory(URI.create(Namespaces.DATASET_NAMESPACE + datasetUuid));
  }

  @Test
  public void testExecuteHeadQuery() throws Exception {
    String query = "Select * where { ?a ?b ?c}";
    URI trialverseDatasetUri = new URI(Namespaces.DATASET_NAMESPACE + datasetUuid);
    HttpResponse httpResponse = new BasicHttpResponse(new BasicStatusLine(new HttpVersion(1, 1), HttpStatus.OK.value(), "reason"));
    String acceptValue = "c/d";
    httpResponse.setHeader("Content-Type", acceptValue);

    String responseStr = "whatevs";
    when(datasetReadRepository.executeQuery(query, trialverseDatasetUri, null, acceptValue)).thenReturn(responseStr.getBytes());
    mockMvc.perform(get("/users/user-name-hash/datasets/" + datasetUuid + "/query")
            .param("query", query)
            .header("Accept", acceptValue)
            .principal(user))
            .andExpect(status().isOk())
            .andExpect(content().contentType(acceptValue));

    verify(datasetReadRepository).executeQuery(query, trialverseDatasetUri, null, acceptValue);
    verify(trialverseIOUtilsService).writeContentToServletResponse(any(byte[].class), Matchers.any(HttpServletResponse.class));

  }

  @Test
  public void testExecuteVersionedQuery() throws Exception {
    String query = "Select * where { ?a ?b ?c}";
    String versionUuid = "my-version";
    URI version = WebConstants.buildVersionUri(versionUuid);
    URI trialverseDatasetUri = new URI(Namespaces.DATASET_NAMESPACE + datasetUuid);
    String acceptValue = "c/d";
    String responseStr = "foo";
    when(datasetReadRepository.executeQuery(query, trialverseDatasetUri, version, acceptValue)).thenReturn(responseStr.getBytes());
    mockMvc.perform(get("/users/user-name-hash/datasets/" + datasetUuid + "/versions/" + versionUuid + "/query")
            .param("query", query)
            .header("Accept", acceptValue)
            .principal(user))
            .andExpect(status().isOk())
            .andExpect(content().contentType(acceptValue));

    verify(datasetReadRepository).executeQuery(query, trialverseDatasetUri, version, acceptValue);
    verify(trialverseIOUtilsService).writeContentToServletResponse(any(byte[].class), Matchers.any(HttpServletResponse.class));
  }

  @Test
  public void testEditDataset() throws Exception {
    String newTitle = "new title";
    String newDescription = "new desc";
    DatasetCommand datasetCommand = new DatasetCommand(newTitle, newDescription);
    String jsonContent = Utils.createJson(datasetCommand);
    URI datasetUri = URI.create(Namespaces.DATASET_NAMESPACE + datasetUuid);
    String newVersion = "newVersion";
    VersionMapping versionMapping = new VersionMapping(1, "http://versioned/" + datasetUuid, john.getId().toString(), datasetUri.toString(), false, null);
    when(versionMappingRepository.getVersionMappingByDatasetUrl(datasetUri)).thenReturn(versionMapping);
    when(datasetWriteRepository.editDataset(principal, versionMapping, newTitle, newDescription)).thenReturn(newVersion);
    mockMvc.perform(post("/users/1/datasets/" + datasetUuid)
            .contentType(WebContent.contentTypeJSON)
            .content(jsonContent)
            .principal(user))
            .andExpect(status().isOk())
            .andExpect(header().string(WebConstants.X_EVENT_SOURCE_VERSION, newVersion));
    verify(datasetService).checkDatasetOwner(1, user);
    verify(versionMappingRepository).getVersionMappingByDatasetUrl(datasetUri);
    verify(datasetWriteRepository).editDataset(principal, versionMapping, newTitle, newDescription);
  }

  @Test
  public void testGetSpecificVersionInfo() throws Exception {
    String versionUuid = "someVersion";
    URI datasetUri = URI.create(Namespaces.DATASET_NAMESPACE + datasetUuid);
    URI versionUri = URI.create(WebConstants.getVersionBaseUri() + versionUuid);

    VersionNode versionNode = new VersionNode("Uri", "title", null, "desc", "creatore", 1, 2, "application");

    when(historyService.getVersionInfo(datasetUri, versionUri)).thenReturn(versionNode);

    mockMvc.perform(get("/users/1/datasets/" + datasetUuid + "/history/" + versionUuid).principal(user))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.versionTitle").value(versionNode.getVersionTitle()))
    ;

    verify(historyService).getVersionInfo(datasetUri, versionUri);
  }

  @Test
  public void testSetArchivedStatus() throws Exception {
    Boolean archived = true;
    DatasetArchiveCommand command = new DatasetArchiveCommand(archived);
    String jsonContent = Utils.createJson(command);
    URI datasetUri = URI.create(Namespaces.DATASET_NAMESPACE + datasetUuid);
    mockMvc.perform(post("/users/1/datasets/" + datasetUuid + "/setArchivedStatus")
            .contentType(WebContent.contentTypeJSON)
            .content(jsonContent)
            .principal(user))
            .andExpect(status().isOk())
    ;
    verify(versionMappingRepository).setArchivedStatus(datasetUri, archived);
    verify(datasetService).checkDatasetOwner(1, user);
  }

  @Test
  public void testSetArchivedStatusNotAllowed() throws Exception {
    DatasetArchiveCommand command = new DatasetArchiveCommand(true);
    String jsonContent = Utils.createJson(command);

    doThrow(MethodNotAllowedException.class).when(datasetService).checkDatasetOwner(2, user);
    mockMvc.perform(post("/users/2/datasets/" + datasetUuid + "/setArchivedStatus")
            .contentType(WebContent.contentTypeJSON)
            .content(jsonContent)
            .principal(user))
    ;
    verify(datasetService).checkDatasetOwner(2, user);
  }
}

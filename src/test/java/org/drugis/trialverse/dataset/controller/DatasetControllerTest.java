package org.drugis.trialverse.dataset.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jdk.nashorn.internal.parser.JSONParser;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFLanguages;
import org.drugis.trialverse.dataset.controller.command.DatasetCommand;
import org.drugis.trialverse.dataset.model.Dataset;
import org.drugis.trialverse.dataset.repository.DatasetReadRepository;
import org.drugis.trialverse.dataset.repository.DatasetWriteRepository;
import org.drugis.trialverse.dataset.service.DatasetService;
import org.drugis.trialverse.dataset.service.HistoryService;
import org.drugis.trialverse.graph.service.GraphService;
import org.drugis.trialverse.security.Account;
import org.drugis.trialverse.security.repository.AccountRepository;
import org.drugis.trialverse.util.Utils;
import org.drugis.trialverse.util.Namespaces;
import org.drugis.trialverse.util.WebConstants;
import org.drugis.trialverse.util.service.TrialverseIOUtilsService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URI;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
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
  private GraphService graphService;

  @Mock
  private WebConstants webConstants;

  @Mock
  private HistoryService historyService;

  @Mock
  private DatasetService datasetService;

  @Inject
  private WebApplicationContext webApplicationContext;

  @InjectMocks
  private DatasetController datasetController;


  private Principal user;
  private Account john = new Account(1, "john@apple.co.uk", "John", "Lennon", "hash");

  @Before
  public void setUp() {
    accountRepository = mock(AccountRepository.class);
    datasetWriteRepository = mock(DatasetWriteRepository.class);
    datasetController = new DatasetController();
    webConstants = mock(WebConstants.class);

    initMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(datasetController).build();
    user = mock(Principal.class);
    when(user.getName()).thenReturn(john.getUsername());
    when(accountRepository.findAccountByUsername("john@apple.co.uk")).thenReturn(john);
  }


  @After
  public void tearDown() {
    verifyNoMoreInteractions(accountRepository, datasetWriteRepository);
  }

  @Test
  public void testCreateDatasetForNonLoginUser() throws Exception {
    String newDatasetUri = "http://some.thing.like/this/asd123";
    URI uri = new URI(newDatasetUri);
    DatasetCommand datasetCommand = new DatasetCommand("dataset title");
    String jsonContent = Utils.createJson(datasetCommand);
    when(datasetWriteRepository.createDataset(datasetCommand.getTitle(), datasetCommand.getDescription(), john)).thenReturn(uri);
    mockMvc
            .perform(post("/users/some-user-uid/datasets")
                            .principal(user)
                            .content(jsonContent)
                            .contentType(webConstants.getApplicationJsonUtf8())
            )
            .andExpect(status().isInternalServerError());

    verify(accountRepository).findAccountByUsername(john.getUsername());
    verifyNoMoreInteractions(datasetWriteRepository);
  }

  @Test
  public void testCreateDataset() throws Exception {
    String newDatasetUri = "http://some.thing.like/this/asd123";
    URI uri = new URI(newDatasetUri);
    DatasetCommand datasetCommand = new DatasetCommand("dataset title");
    String jsonContent = Utils.createJson(datasetCommand);
    when(datasetWriteRepository.createDataset(datasetCommand.getTitle(), datasetCommand.getDescription(), john)).thenReturn(uri);
    String userhash = john.getuserNameHash();
    mockMvc
            .perform(post("/users/" + userhash + "/datasets")
                            .principal(user)
                            .content(jsonContent)
                            .contentType(webConstants.getApplicationJsonUtf8())
            )
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", newDatasetUri));

    verify(accountRepository).findAccountByUsername(john.getUsername());
    verify(datasetWriteRepository).createDataset(datasetCommand.getTitle(), datasetCommand.getDescription(), john);
  }

  @Test
  public void queryDatasetsRequestPathTurtleType() throws Exception {
    Model model = mock(Model.class);
    when(datasetReadRepository.queryDatasets(john)).thenReturn(model);
    String userNameHash = "userNameHash";
    when(accountRepository.findAccountByHash(userNameHash)).thenReturn(john);

    mockMvc.perform(get("/users/" + userNameHash + "/datasets").principal(user)
            .accept(RDFLanguages.TURTLE.getHeaderString()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(RDFLanguages.TURTLE.getContentType().getContentType()));

    verify(accountRepository).findAccountByHash(userNameHash);
    verify(datasetReadRepository).queryDatasets(john);
    verify(trialverseIOUtilsService).writeModelToServletResponse(Matchers.any(Model.class), Matchers.any(HttpServletResponse.class));
  }

  @Test
  public void queryDatasetsRequestPathJsonType() throws Exception {
    List<Dataset> datasets = Arrays.asList(new Dataset("uri", john, "title", "description"));
    when(datasetService.findDatasets(john)).thenReturn(datasets);
    String userNameHash = "userNameHash";
    when(accountRepository.findAccountByHash(userNameHash)).thenReturn(john);

    mockMvc.perform(get("/users/" + userNameHash + "/datasets").principal(user)
            .accept(WebConstants.getApplicationJsonUtf8()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.getApplicationJsonUtf8()))
            .andExpect(jsonPath("$", hasSize(datasets.size())));

    verify(accountRepository).findAccountByHash(userNameHash);
    verify(datasetService).findDatasets(john);
  }

  @Test
  public void queryDatasetsGraphs() throws Exception {
    Model model = mock(Model.class);
    HttpServletResponse mockServletResponse = mock(HttpServletResponse.class);
    when(datasetReadRepository.queryDatasets(john)).thenReturn(model);
    String userNameHash = "userNameHash";
    when(accountRepository.findAccountByHash(userNameHash)).thenReturn(john);

    datasetController.queryDatasetsGraphsByUser(mockServletResponse, userNameHash);

    verify(accountRepository).findAccountByHash(userNameHash);
    verify(datasetReadRepository).queryDatasets(john);
    verify(trialverseIOUtilsService).writeModelToServletResponse(model, mockServletResponse);
  }

  @Test
  public void testGetDataset() throws Exception {
    String uuid = "uuuuiiid-yeswecan";
    URI datasetUri = new URI(Namespaces.DATASET_NAMESPACE + uuid);
    Model model = mock(Model.class);
    when(datasetReadRepository.getVersionedDataset(datasetUri, null)).thenReturn(model);
    when(accountRepository.findAccountByUsername(user.getName())).thenReturn(john);

    mockMvc.perform((get("/users/some-user-uid/datasets/" + uuid)).principal(user))
            .andExpect(status().isOk())
            .andExpect(content().contentType(RDFLanguages.TURTLE.getContentType().getContentType()));

    verify(datasetReadRepository).getVersionedDataset(datasetUri, null);
    verify(trialverseIOUtilsService).writeModelToServletResponse(Matchers.any(Model.class), Matchers.any(HttpServletResponse.class));
  }

  @Test
  public void testGetDatasetVersion() throws Exception {
    String uuid = "uuuuiiid-yeswecan";
    String versionUuid = "versionUuid";

    URI datasetUri = new URI(Namespaces.DATASET_NAMESPACE + uuid);
    Model model = mock(Model.class);
    when(datasetReadRepository.getVersionedDataset(datasetUri, versionUuid)).thenReturn(model);
    when(accountRepository.findAccountByUsername(user.getName())).thenReturn(john);

    mockMvc.perform((get("/users/user-name-hash/datasets/" + uuid + "/versions/" + versionUuid)).principal(user))
            .andExpect(status().isOk())
            .andExpect(content().contentType(RDFLanguages.TURTLE.getContentType().getContentType()));

    verify(datasetReadRepository).getVersionedDataset(datasetUri, versionUuid);
    verify(trialverseIOUtilsService).writeModelToServletResponse(Matchers.any(Model.class), Matchers.any(HttpServletResponse.class));
  }

  @Test
  public void testGetHistory() throws Exception {
    String uuid = "uuuuiiid-yeswecan";
    URI datasetUri = new URI(Namespaces.DATASET_NAMESPACE + uuid);
    HttpResponse httpResponse = new BasicHttpResponse(new BasicStatusLine(new HttpVersion(1, 1), HttpStatus.OK.value(), "reason"));
    String test = "test";
    httpResponse.setEntity(new StringEntity(test));
    Model historyModel = ModelFactory.createDefaultModel();
    InputStream historyStream = new ClassPathResource("mockMergeHistory.ttl").getInputStream();
    historyModel.read(historyStream, null, "TTL");

    mockMvc.perform((get("/users/user-name-hash/datasets/" + uuid + "/versions")).principal(user))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json;charset=UTF-8"));

  }

  @Test
  public void testExecuteHeadQuery() throws Exception {
    String uuid = "uuuuiiid-yeswecan";
    String query = "Select * where { ?a ?b ?c}";
    URI trialverseDatasetUri = new URI(Namespaces.DATASET_NAMESPACE + uuid);
    HttpResponse httpResponse = new BasicHttpResponse(new BasicStatusLine(new HttpVersion(1, 1), HttpStatus.OK.value(), "reason"));
    String acceptValue = "c/d";
    httpResponse.setHeader("Content-Type", acceptValue);

    String responseStr = "whatevs";
    when(datasetReadRepository.executeQuery(query, trialverseDatasetUri, null, acceptValue)).thenReturn(responseStr.getBytes());
    mockMvc.perform((get("/users/user-name-hash/datasets/" + uuid + "/query"))
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
    String uuid = "uuuuiiid-yeswecan";
    String query = "Select * where { ?a ?b ?c}";
    String version = "my-version";
    URI trialverseDatasetUri = new URI(Namespaces.DATASET_NAMESPACE + uuid);
    String acceptValue = "c/d";
    String responseStr = "foo";
    when(datasetReadRepository.executeQuery(query, trialverseDatasetUri, version, acceptValue)).thenReturn(responseStr.getBytes());
    mockMvc.perform((get("/users/user-name-hash/datasets/" + uuid + "/versions/" + version + "/query"))
            .param("query", query)
            .header("Accept", acceptValue)
            .principal(user))
            .andExpect(status().isOk())
            .andExpect(content().contentType(acceptValue));

    verify(datasetReadRepository).executeQuery(query, trialverseDatasetUri, version, acceptValue);
    verify(trialverseIOUtilsService).writeContentToServletResponse(any(byte[].class), Matchers.any(HttpServletResponse.class));

  }
}

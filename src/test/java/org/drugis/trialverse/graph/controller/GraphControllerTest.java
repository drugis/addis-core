package org.drugis.trialverse.graph.controller;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.apache.jena.riot.RDFLanguages;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.repository.DatasetReadRepository;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.graph.repository.GraphReadRepository;
import org.drugis.trialverse.graph.repository.GraphWriteRepository;
import org.drugis.trialverse.graph.service.GraphService;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.trialverse.util.Utils;
import org.drugis.trialverse.util.Namespaces;
import org.drugis.trialverse.util.Utils;
import org.drugis.trialverse.util.WebConstants;
import org.drugis.trialverse.util.service.TrialverseIOUtilsService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.security.Principal;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Configuration
@EnableWebMvc
public class GraphControllerTest {

  private MockMvc mockMvc;

  @Inject
  private WebApplicationContext webApplicationContext;

  @Mock
  private WebConstants webConstants;

  @Mock
  private GraphWriteRepository graphWriteRepository;

  @Mock
  private GraphReadRepository graphReadRepository;

  @Mock
  private DatasetReadRepository datasetReadRepository;

  @Mock
  private TrialverseIOUtilsService trialverseIOUtilsService;

  @Mock
  private AccountRepository accountRepository;

  @Mock
  private GraphService graphService;

  @Mock
  private VersionMappingRepository versionMappingRepository;

  @InjectMocks
  private GraphController graphController;

  private String userHash = "userHash";
  private Account john = new Account(1, "john@apple.co.uk", "John", "Lennon", userHash);
  private Principal user;

  @Before
  public void setUp() throws Exception {
    graphReadRepository = mock(GraphReadRepository.class);
    graphWriteRepository = mock(GraphWriteRepository.class);
    datasetReadRepository = mock(DatasetReadRepository.class);
    graphController = new GraphController();

    initMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(graphController).build();
    user = mock(Principal.class);
    when(user.getName()).thenReturn(john.getUsername());
    when(accountRepository.findAccountByUsername(john.getUsername())).thenReturn(john);
  }

  @After
  public void tearDown() throws Exception {
    verifyNoMoreInteractions(graphWriteRepository);
  }

  @Test
  public void testGetGraph() throws Exception {
    String datasetUuid = "datasetUUID";
    String graphUUID = "graphUUID";
    String versionUuid = "versionUuid";
    String versionedDatasetUrl = "http://myversiondDatasetUrl";
    String responce = "responce";
    URI trialverseDatasetUrl = new URI(Namespaces.DATASET_NAMESPACE + datasetUuid);
    VersionMapping versionMapping = new VersionMapping(versionedDatasetUrl, "anyOwner", trialverseDatasetUrl.toString());
    when(versionMappingRepository.getVersionMappingByDatasetUrl(trialverseDatasetUrl)).thenReturn(versionMapping);
    when(graphReadRepository.getGraph(versionedDatasetUrl, versionUuid, graphUUID, WebConstants.TURTLE)).thenReturn(responce.getBytes());

    mockMvc.perform(get("/users/" + userHash + "/datasets/" + datasetUuid + "/versions/" + versionUuid + "/graphs/" + graphUUID)
            .principal(user)
            .header("Accept", WebConstants.TURTLE))
            .andExpect(status().isOk())
            .andExpect(content().contentType(RDFLanguages.TURTLE.getContentType().getContentType()));

    verify(graphReadRepository).getGraph(versionedDatasetUrl, versionUuid, graphUUID, WebConstants.TURTLE);
    verify(trialverseIOUtilsService).writeContentToServletResponse(any(byte[].class), any(HttpServletResponse.class));
  }

  @Test
  public void testCreateGraphUserIsNotDatasetOwner() throws Exception {
    String jsonContent = Utils.loadResource(this.getClass(), "/mockStudy.json");
    String datasetUUID = "datasetUUID";
    URI datasetUri = new URI(Namespaces.DATASET_NAMESPACE + datasetUUID);
    String graphUUID = "graphUUID";
    when(datasetReadRepository.isOwner(datasetUri, user)).thenReturn(false);

    mockMvc.perform(
            put("/users/" + userHash + "/datasets/" + datasetUUID + "/graphs/" + graphUUID)
                    .content(jsonContent)
                    .contentType(WebConstants.JSON_LD)
                    .param(WebConstants.COMMIT_TITLE_PARAM, "test title header")
                    .principal(user)).andExpect(status().isForbidden());

    verify(datasetReadRepository).isOwner(datasetUri, user);
    verifyZeroInteractions(graphWriteRepository);
  }

  @Test
  public void testUpdateJsonGraph() throws Exception {
    String updateContent = "updateContent";
    String datasetUUID = "datasetUUID";
    URI datasetUrl = new URI(Namespaces.DATASET_NAMESPACE + datasetUUID);
    String graphUUID = "graphUUID";

    when(datasetReadRepository.isOwner(datasetUrl, user)).thenReturn(true);
    Header versionHeader = new BasicHeader(WebConstants.X_EVENT_SOURCE_VERSION, "http://myVersion");
    when(graphWriteRepository.updateGraph(Matchers.<URI>anyObject(), anyString(), any(InputStream.class), anyString(), anyString())).thenReturn(versionHeader);

    mockMvc.perform(
            put("/users/" + userHash + "/datasets/" + datasetUUID + "/graphs/" + graphUUID)
                    .content(updateContent)
                    .contentType(WebConstants.JSON_LD)
                    .param(WebConstants.COMMIT_TITLE_PARAM, "test title header")
                    .param(WebConstants.COMMIT_DESCRIPTION_PARAM, "description")
                    .principal(user))
            .andExpect(status().isOk())
            .andExpect(header().string(WebConstants.X_EVENT_SOURCE_VERSION, "http://myVersion"));

    verify(graphService).jsonGraphInputStreamToTurtleInputStream( any(InputStream.class));
    verify(datasetReadRepository).isOwner(datasetUrl, user);
    verify(graphWriteRepository).updateGraph(Matchers.<URI>anyObject(), anyString(), any(InputStream.class), anyString(), anyString());
  }

  @Test
  public void testUpdateGraph() throws Exception {
    String updateContent = "updateContent";
    String datasetUUID = "datasetUUID";
    URI datasetUrl = new URI(Namespaces.DATASET_NAMESPACE + datasetUUID);
    String graphUUID = "graphUUID";

    when(datasetReadRepository.isOwner(datasetUrl, user)).thenReturn(true);
    Header versionHeader = new BasicHeader(WebConstants.X_EVENT_SOURCE_VERSION, "http://myVersion");
    when(graphWriteRepository.updateGraph(Matchers.<URI>anyObject(), anyString(), any(InputStream.class), anyString(), anyString())).thenReturn(versionHeader);

    mockMvc.perform(
            put("/users/" + userHash + "/datasets/" + datasetUUID + "/graphs/" + graphUUID)
                    .content(updateContent)
                    .contentType(WebConstants.JSON_LD)
                    .param(WebConstants.COMMIT_TITLE_PARAM, "test title header")
                    .param(WebConstants.COMMIT_DESCRIPTION_PARAM, "description")
                    .principal(user))
            .andExpect(status().isOk())
            .andExpect(header().string(WebConstants.X_EVENT_SOURCE_VERSION, "http://myVersion"));

    verify(datasetReadRepository).isOwner(datasetUrl, user);
    verify(graphWriteRepository).updateGraph(Matchers.<URI>anyObject(), anyString(), any(InputStream.class), anyString(), anyString());
  }

  @Test
  public void testCopyGraph() throws Exception {
    String datasetUuid = "datasetUuid";
    String versionUuid = "versionUuid";
    String graphUuid = "graphUuid";
    URI datasetUri = new URI(Namespaces.DATASET_NAMESPACE + datasetUuid);
    URI sourceGraphUri = URI.create("http://testhost/datasets/sourceDatasetUuid/versions/sourceVersionUuid/graphs/sourceGraphUuid");
    String newVersion = "http://myVersion";

    URI targetDatasetUri = URI.create(Namespaces.DATASET_NAMESPACE + datasetUuid);
    String testHost = "http://testhost";
    URI targetGraphUri = URI.create(Namespaces.GRAPH_NAMESPACE + graphUuid);

    when(datasetReadRepository.isOwner(datasetUri, user)).thenReturn(true);
    when(graphService.copy(targetDatasetUri, targetGraphUri, sourceGraphUri)).thenReturn(URI.create(newVersion));

    mockMvc.perform(
            put("/users/" + userHash + "/datasets/" + datasetUuid + "/graphs/" + graphUuid)
                    .content("")
                    .param(WebConstants.COPY_OF_QUERY_PARAM, sourceGraphUri.toString())
                    .principal(user))
            .andExpect(status().isOk())
            .andExpect(header().string(WebConstants.X_EVENT_SOURCE_VERSION, newVersion));

    verify(datasetReadRepository).isOwner(datasetUri, user);
  }


  @Test
  public void testUpdateGraphUserNotDatasetOwner() throws Exception {
    String jsonContent = Utils.loadResource(this.getClass(), "/mockStudy.json");
    String datasetUUID = "datasetUUID";
    URI datasetUrl = new URI(Namespaces.DATASET_NAMESPACE + datasetUUID);
    String graphUUID = "graphUUID";
    when(datasetReadRepository.isOwner(datasetUrl, user)).thenReturn(false);

    mockMvc.perform(
            put("/users/" + userHash + "/datasets/" + datasetUUID + "/graphs/" + graphUUID)
                    .content(jsonContent)
                    .contentType(WebConstants.JSON_LD)
                    .param(WebConstants.COMMIT_TITLE_PARAM, "test title header")
                    .principal(user)).andExpect(status().isForbidden());

    verify(datasetReadRepository).isOwner(datasetUrl, user);
  }
}

package org.drugis.trialverse.graph.controller;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.apache.jena.riot.RDFLanguages;
import org.drugis.addis.importer.service.ClinicalTrialsImportService;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.util.WebConstants;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.repository.DatasetReadRepository;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.graph.exception.DeleteGraphException;
import org.drugis.trialverse.graph.repository.GraphReadRepository;
import org.drugis.trialverse.graph.repository.GraphWriteRepository;
import org.drugis.trialverse.graph.service.GraphService;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.util.NestedServletException;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.security.Principal;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Configuration
@EnableWebMvc
public class GraphControllerTest {

  private MockMvc mockMvc;

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

  @Mock
  private ClinicalTrialsImportService clinicalTrialsImportService;

  @InjectMocks
  private GraphController graphController;

  private String userHash = "userHash";
  private Account john = new Account(1, "john@apple.co.uk", "John", "Lennon", userHash);
  private Principal user;

  @Before
  public void setUp() {
    graphController = new GraphController();

    initMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(graphController).build();
    user = mock(Principal.class);
    when(user.getName()).thenReturn(john.getUsername());
    when(accountRepository.findAccountByUsername(john.getUsername())).thenReturn(john);
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(graphWriteRepository);
  }

  @Test
  public void testCreateGraphJson() throws Exception {
    String newGraph = "{}";
    String datasetUuid = "datasetUuid";
    String graphUuid = "graphUuid";
    String commitTitle = "commit+title";
    String commitDescription = "commit+description";
    URI trialverseDatasetUri = new URI(Namespaces.DATASET_NAMESPACE + datasetUuid);
    InputStream inputStream = new ByteArrayInputStream(new byte[]{});
    Header versionHeader = new BasicHeader("version", "http://trials.drugis.org/versions/3012");
    VersionMapping mapping = new VersionMapping("any-versioned-url", "someone", trialverseDatasetUri.toString());

    when(datasetReadRepository.isOwner(trialverseDatasetUri, user)).thenReturn(true);
    when(graphService.jsonGraphInputStreamToTurtleInputStream(any())).thenReturn(inputStream);
    when(graphWriteRepository.updateGraph(Matchers.anyObject(), anyString(), any(InputStream.class), anyString(), anyString())).thenReturn(versionHeader);
    when(versionMappingRepository.getVersionMappingByDatasetUrl(trialverseDatasetUri)).thenReturn(mapping);

    mockMvc.perform(put("/users/" + userHash + "/datasets/" + datasetUuid + "/graphs/" + graphUuid)
            .principal(user)
            .content(newGraph)
            .param(WebConstants.COMMIT_TITLE_PARAM, commitTitle)
            .param(WebConstants.COMMIT_DESCRIPTION_PARAM, commitDescription)
            .contentType(WebConstants.JSON_LD))
            .andExpect(status().isOk())
            .andExpect(header().string(WebConstants.X_EVENT_SOURCE_VERSION, versionHeader.getValue()));

    verify(versionMappingRepository).getVersionMappingByDatasetUrl(trialverseDatasetUri);
    verify(datasetReadRepository).isOwner(trialverseDatasetUri, user);
    verify(graphWriteRepository).updateGraph(Matchers.anyObject(), anyString(), any(InputStream.class), anyString(), anyString());
  }

  @Test
  public void testCreateGraphTurtle() throws Exception {
    String newGraph = "<a> <b> <c>";
    String datasetUuid = "datasetUuid";
    String graphUuid = "graphUuid";
    String commitTitle = "commit+title";
    String commitDescription = "commit+description";
    URI trialverseDatasetUri = new URI(Namespaces.DATASET_NAMESPACE + datasetUuid);
    Header versionHeader = new BasicHeader("version", "http://trials.drugis.org/versions/3012");
    VersionMapping mapping = new VersionMapping("any-versioned-url", "someone", trialverseDatasetUri.toString());

    when(datasetReadRepository.isOwner(trialverseDatasetUri, user)).thenReturn(true);
    when(graphWriteRepository.updateGraph(Matchers.anyObject(), anyString(), any(InputStream.class), anyString(), anyString())).thenReturn(versionHeader);
    when(versionMappingRepository.getVersionMappingByDatasetUrl(trialverseDatasetUri)).thenReturn(mapping);

    mockMvc.perform(put("/users/" + userHash + "/datasets/" + datasetUuid + "/graphs/" + graphUuid)
            .principal(user)
            .content(newGraph)
            .param(WebConstants.COMMIT_TITLE_PARAM, commitTitle)
            .param(WebConstants.COMMIT_DESCRIPTION_PARAM, commitDescription)
            .contentType(WebConstants.JSON_LD))
            .andExpect(status().isOk())
            .andExpect(header().string(WebConstants.X_EVENT_SOURCE_VERSION, versionHeader.getValue()));

    verify(versionMappingRepository).getVersionMappingByDatasetUrl(trialverseDatasetUri);
    verify(datasetReadRepository).isOwner(trialverseDatasetUri, user);
    verify(graphWriteRepository).updateGraph(Matchers.anyObject(), anyString(), any(InputStream.class), anyString(), anyString());
  }

  @Test
  public void testGetGraph() throws Exception {
    String datasetUuid = "datasetUuid";
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

  @Test(expected = NestedServletException.class)
  public void testCreateGraphUserIsNotDatasetOwner() throws Exception {
    String jsonContent = Utils.loadResource(this.getClass(), "/mockStudy.json");
    String datasetUuid = "datasetUuid";
    URI datasetUri = new URI(Namespaces.DATASET_NAMESPACE + datasetUuid);
    String graphUUID = "graphUUID";
    when(datasetReadRepository.isOwner(datasetUri, user)).thenReturn(false);

    mockMvc.perform(
            put("/users/" + userHash + "/datasets/" + datasetUuid + "/graphs/" + graphUUID)
                    .content(jsonContent)
                    .contentType(WebConstants.JSON_LD)
                    .param(WebConstants.COMMIT_TITLE_PARAM, "test title header")
                    .principal(user))
            .andExpect(status().isForbidden());

    verify(datasetReadRepository).isOwner(datasetUri, user);
    verifyZeroInteractions(graphWriteRepository);
  }

  @Test
  public void testImportStudy() throws Exception {

    String datasetUuid = "datasetUuid";
    URI datasetUri = new URI(Namespaces.DATASET_NAMESPACE + datasetUuid);
    String graphUUID = "graphUUID";
    String studyRef = "123ABC";
    String title = "test title header";
    when(datasetReadRepository.isOwner(datasetUri, user)).thenReturn(true);
    String versionValue = "http://myVersion";
    Header mockHeader = mock(Header.class);
    when(mockHeader.getValue()).thenReturn(versionValue);

    String trialverseDatasetUri = "trialverseDatasetUri";
    when(versionMappingRepository.getVersionMappingByDatasetUrl(datasetUri)).thenReturn(new VersionMapping(trialverseDatasetUri, "ownerUuid", "addisUri"));
    when(clinicalTrialsImportService.importStudy(title, null, trialverseDatasetUri, graphUUID, studyRef)).thenReturn(mockHeader);

    mockMvc.perform(
            post("/users/" + userHash + "/datasets/" + datasetUuid + "/graphs/" + graphUUID + "/import/" + studyRef)
                    .param(WebConstants.COMMIT_TITLE_PARAM, title)
                    .principal(user))
            .andExpect(status().isOk())
            .andExpect(header().string(WebConstants.X_EVENT_SOURCE_VERSION, versionValue));

    verify(datasetReadRepository).isOwner(datasetUri, user);
    verify(clinicalTrialsImportService).importStudy(title, null, trialverseDatasetUri, graphUUID, studyRef);
  }

  @Test
  public void testImportEudract() throws Exception {
    String datasetUuid = "datasetUuid";
    URI datasetUri = new URI(Namespaces.DATASET_NAMESPACE + datasetUuid);
    when(datasetReadRepository.isOwner(datasetUri, user)).thenReturn(true);
    String versionValue = "http://myVersion";
    Header mockHeader = mock(Header.class);
    when(mockHeader.getValue()).thenReturn(versionValue);
    when(versionMappingRepository.getVersionMappingByDatasetUrl(datasetUri)).thenReturn(new VersionMapping(datasetUri.toString(), "ownerUuid", "addisUri"));
    when(clinicalTrialsImportService.importEudract(eq(datasetUri.toString()), anyString(), any())).thenReturn(mockHeader);

    mockMvc.perform(
            post("/users/" + userHash + "/datasets/" + datasetUuid + "/graphs/import-eudract")
                    .contentType(MediaType.APPLICATION_XML_VALUE)
                    .content("foo")
                    .principal(user))
            .andExpect(status().isOk())
            .andExpect(header().string(WebConstants.X_EVENT_SOURCE_VERSION, versionValue));

    verify(datasetReadRepository).isOwner(datasetUri, user);
    verify(clinicalTrialsImportService).importEudract(eq(datasetUri.toString()), anyString(), any());
  }

  @Test
  public void testUpdateJsonGraph() throws Exception {
    String updateContent = "updateContent";
    String datasetUuid = "datasetUuid";
    URI datasetUri = new URI(Namespaces.DATASET_NAMESPACE + datasetUuid);
    String graphUUID = "graphUUID";

    when(datasetReadRepository.isOwner(datasetUri, user)).thenReturn(true);
    Header versionHeader = new BasicHeader(WebConstants.X_EVENT_SOURCE_VERSION, "http://myVersion");
    when(graphWriteRepository.updateGraph(Matchers.anyObject(), anyString(), any(InputStream.class), anyString(), anyString())).thenReturn(versionHeader);
    String versionedUrl = "anything";
    VersionMapping mapping = new VersionMapping(versionedUrl, "owner", datasetUri.toString());
    when(versionMappingRepository.getVersionMappingByDatasetUrl(datasetUri)).thenReturn(mapping);
    mockMvc.perform(
            put("/users/" + userHash + "/datasets/" + datasetUuid + "/graphs/" + graphUUID)
                    .content(updateContent)
                    .contentType(WebConstants.JSON_LD)
                    .param(WebConstants.COMMIT_TITLE_PARAM, "test title header")
                    .param(WebConstants.COMMIT_DESCRIPTION_PARAM, "description")
                    .principal(user))
            .andExpect(status().isOk())
            .andExpect(header().string(WebConstants.X_EVENT_SOURCE_VERSION, "http://myVersion"));

    verify(graphService).jsonGraphInputStreamToTurtleInputStream(any(InputStream.class));
    verify(datasetReadRepository).isOwner(datasetUri, user);
    verify(graphWriteRepository).updateGraph(Matchers.anyObject(), anyString(), any(InputStream.class), anyString(), anyString());
    verify(versionMappingRepository).getVersionMappingByDatasetUrl(datasetUri);
  }

  @Test
  public void testUpdateGraph() throws Exception {
    String updateContent = "updateContent";
    String datasetUuid = "datasetUuid";
    URI datasetUri = new URI(Namespaces.DATASET_NAMESPACE + datasetUuid);
    String versionedUrl = "anything";
    String graphUUID = "graphUUID";

    VersionMapping mapping = new VersionMapping(versionedUrl, "someone", datasetUri.toString());
    when(versionMappingRepository.getVersionMappingByDatasetUrl(datasetUri)).thenReturn(mapping);

    when(datasetReadRepository.isOwner(datasetUri, user)).thenReturn(true);
    Header versionHeader = new BasicHeader(WebConstants.X_EVENT_SOURCE_VERSION, "http://myVersion");
    when(graphWriteRepository.updateGraph(Matchers.anyObject(), anyString(), any(InputStream.class), anyString(), anyString())).thenReturn(versionHeader);

    mockMvc.perform(
            put("/users/" + userHash + "/datasets/" + datasetUuid + "/graphs/" + graphUUID)
                    .content(updateContent)
                    .contentType(WebConstants.JSON_LD)
                    .param(WebConstants.COMMIT_TITLE_PARAM, "test title header")
                    .param(WebConstants.COMMIT_DESCRIPTION_PARAM, "description")
                    .principal(user))
            .andExpect(status().isOk())
            .andExpect(header().string(WebConstants.X_EVENT_SOURCE_VERSION, "http://myVersion"));

    verify(datasetReadRepository).isOwner(datasetUri, user);
    verify(graphWriteRepository).updateGraph(Matchers.anyObject(), anyString(), any(InputStream.class), anyString(), anyString());
    verify(versionMappingRepository).getVersionMappingByDatasetUrl(datasetUri);
  }

  @Test
  public void testCopyGraph() throws Exception {
    String datasetUuid = "datasetUuid";
    String graphUuid = "graphUuid";
    URI datasetUri = new URI(Namespaces.DATASET_NAMESPACE + datasetUuid);
    URI sourceGraphUri = URI.create("http://testhost/datasets/sourceDatasetUuid/versions/sourceVersionUuid/graphs/sourceGraphUuid");
    String newVersion = "http://myVersion";

    URI targetDatasetUri = URI.create(Namespaces.DATASET_NAMESPACE + datasetUuid);
    URI targetGraphUri = URI.create(Namespaces.GRAPH_NAMESPACE + graphUuid);

    VersionMapping mapping = new VersionMapping(targetDatasetUri.toString(), "someone", datasetUri.toString());
    when(versionMappingRepository.getVersionMappingByDatasetUrl(datasetUri)).thenReturn(mapping);

    when(datasetReadRepository.isOwner(datasetUri, user)).thenReturn(true);
    when(graphService.copy(targetDatasetUri, targetGraphUri, sourceGraphUri)).thenReturn(URI.create(newVersion));
    when(graphService.buildGraphUri(graphUuid)).thenReturn(targetGraphUri);
    mockMvc.perform(
            put("/users/" + userHash + "/datasets/" + datasetUuid + "/graphs/" + graphUuid)
                    .content("")
                    .param(WebConstants.COPY_OF_QUERY_PARAM, sourceGraphUri.toString())
                    .principal(user))
            .andExpect(status().isOk())
            .andExpect(header().string(WebConstants.X_EVENT_SOURCE_VERSION, newVersion));

    verify(datasetReadRepository).isOwner(datasetUri, user);
  }


  @Test(expected = NestedServletException.class)
  public void testUpdateGraphUserNotDatasetOwner() throws Exception {
    String jsonContent = Utils.loadResource(this.getClass(), "/mockStudy.json");
    String datasetUuid = "datasetUuid";
    URI datasetUrl = new URI(Namespaces.DATASET_NAMESPACE + datasetUuid);
    String graphUUID = "graphUUID";
    when(datasetReadRepository.isOwner(datasetUrl, user)).thenReturn(false);

    mockMvc.perform(
            put("/users/" + userHash + "/datasets/" + datasetUuid + "/graphs/" + graphUUID)
                    .content(jsonContent)
                    .contentType(WebConstants.JSON_LD)
                    .param(WebConstants.COMMIT_TITLE_PARAM, "test title header")
                    .principal(user)).andExpect(status().isForbidden());

    verify(datasetReadRepository).isOwner(datasetUrl, user);
  }

  @Test
  public void testDeleteGraph() throws Exception, DeleteGraphException {
    String datasetUuid = "datasetUuid";
    String graphUUID = "graphUuid";
    URI datasetUri = new URI(Namespaces.DATASET_NAMESPACE + datasetUuid);
    String versionedUrl = "http://versioned/url";
    Header newVersionHeader = new BasicHeader(WebConstants.X_EVENT_SOURCE_VERSION, "newVersion");

    VersionMapping mapping = new VersionMapping(versionedUrl, "someone", datasetUri.toString());
    when(versionMappingRepository.getVersionMappingByDatasetUrl(datasetUri)).thenReturn(mapping);

    when(datasetReadRepository.isOwner(datasetUri, user)).thenReturn(true);
    when(graphWriteRepository.deleteGraph(URI.create(versionedUrl), graphUUID)).thenReturn(newVersionHeader);

    mockMvc.perform(
            delete("/users/" + userHash + "/datasets/" + datasetUuid + "/graphs/" + graphUUID)
                    .principal(user)

    ).andExpect(status().isOk())
            .andExpect(header().string(WebConstants.X_EVENT_SOURCE_VERSION, "newVersion"));

    verify(datasetReadRepository).isOwner(datasetUri, user);
    verify(graphWriteRepository).deleteGraph(URI.create(versionedUrl), graphUUID);
    verify(versionMappingRepository).getVersionMappingByDatasetUrl(datasetUri);
  }

  @Test
  public void testGetHeadConceptsJson() throws Exception {
    String datasetUuid = "datasetUuid";
    String graphUuid = "graphUuid";
    URI datasetUri = new URI(Namespaces.DATASET_NAMESPACE + datasetUuid);
    String versionedUrl = "http://versioned/url";
    VersionMapping versionMapping = new VersionMapping(versionedUrl, "someone", datasetUri.toString());
    String response = "response";
    byte[] responseContent = response.getBytes();

    when(versionMappingRepository.getVersionMappingByDatasetUrl(datasetUri)).thenReturn(versionMapping);
    when(graphReadRepository.getGraph(versionMapping.getVersionUrl(), null, graphUuid, WebConstants.JSON_LD)).thenReturn(responseContent);

    mockMvc.perform(get("/users/" + userHash + "/datasets/" + datasetUuid + "/graphs/" + graphUuid + "/concepts"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.JSON_LD));

    verify(versionMappingRepository).getVersionMappingByDatasetUrl(datasetUri);
    verify(graphReadRepository).getGraph(versionMapping.getVersionUrl(), null, graphUuid, WebConstants.JSON_LD);
    verify(trialverseIOUtilsService).writeContentToServletResponse(any(byte[].class), any(HttpServletResponse.class));
  }

  @Test
  public void testGetVersionedConceptsJson() throws Exception {
    String datasetUuid = "datasetUuid";
    String graphUuid = "graphUuid";
    String versionUuid = "versionUuid";
    URI datasetUri = new URI(Namespaces.DATASET_NAMESPACE + datasetUuid);
    String versionedUrl = "http://versioned/url";
    VersionMapping versionMapping = new VersionMapping(versionedUrl, "someone", datasetUri.toString());
    String response = "response";
    byte[] responseContent = response.getBytes();

    when(versionMappingRepository.getVersionMappingByDatasetUrl(datasetUri)).thenReturn(versionMapping);
    when(graphReadRepository.getGraph(versionMapping.getVersionUrl(), versionUuid, graphUuid, WebConstants.JSON_LD)).thenReturn(responseContent);

    mockMvc.perform(get("/users/" + userHash + "/datasets/" + datasetUuid + "/versions/" + versionUuid +
            "/graphs/" + graphUuid + "/concepts"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.JSON_LD));

    verify(versionMappingRepository).getVersionMappingByDatasetUrl(datasetUri);
    verify(graphReadRepository).getGraph(versionMapping.getVersionUrl(), versionUuid, graphUuid, WebConstants.JSON_LD);
    verify(trialverseIOUtilsService).writeContentToServletResponse(any(byte[].class), any(HttpServletResponse.class));
  }

  @Test
  public void testGetGraphHeadVersionJsonLD() throws Exception {
    String datasetUuid = "datasetUuid";
    String graphUuid = "graphUuid";
    URI datasetUri = new URI(Namespaces.DATASET_NAMESPACE + datasetUuid);
    String versionedUrl = "http://versioned/url";
    VersionMapping versionMapping = new VersionMapping(versionedUrl, "someone", datasetUri.toString());
    String response = "response";
    byte[] responseContent = response.getBytes();

    when(versionMappingRepository.getVersionMappingByDatasetUrl(datasetUri)).thenReturn(versionMapping);
    when(graphReadRepository.getGraph(versionMapping.getVersionUrl(), null, graphUuid, WebConstants.JSON_LD)).thenReturn(responseContent);

    mockMvc.perform(get("/users/" + userHash + "/datasets/" + datasetUuid + "/graphs/" + graphUuid))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.JSON_LD));

    verify(versionMappingRepository).getVersionMappingByDatasetUrl(datasetUri);
    verify(graphReadRepository).getGraph(versionMapping.getVersionUrl(), null, graphUuid, WebConstants.JSON_LD);
    verify(trialverseIOUtilsService).writeContentToServletResponse(any(byte[].class), any(HttpServletResponse.class));
  }

  @Test
  public void testGetGraphHeadVersionJsonLDFailure() throws Exception {
    String datasetUuid = "datasetUuid";
    String graphUuid = "graphUuid";
    URI datasetUri = new URI(Namespaces.DATASET_NAMESPACE + datasetUuid);
    String versionedUrl = "http://versioned/url";
    VersionMapping versionMapping = new VersionMapping(versionedUrl, "someone", datasetUri.toString());
    String response = "{ }\n";
    byte[] responseContent = response.getBytes();

    when(versionMappingRepository.getVersionMappingByDatasetUrl(datasetUri)).thenReturn(versionMapping);
    when(graphReadRepository.getGraph(versionMapping.getVersionUrl(), null, graphUuid, WebConstants.JSON_LD)).thenReturn(responseContent);

    mockMvc.perform(get("/users/" + userHash + "/datasets/" + datasetUuid + "/graphs/" + graphUuid))
            .andExpect(status().isNotFound());

    verify(versionMappingRepository).getVersionMappingByDatasetUrl(datasetUri);
    verify(graphReadRepository).getGraph(versionMapping.getVersionUrl(), null, graphUuid, WebConstants.JSON_LD);
  }
}

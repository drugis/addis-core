package org.drugis.addis.trialverse;


import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.drugis.addis.config.TestConfig;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.trialverse.model.*;
import org.drugis.addis.trialverse.model.emun.StudyDataSection;
import org.drugis.addis.trialverse.service.MappingService;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.drugis.addis.util.WebConstants;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by connor on 2/12/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@WebAppConfiguration
public class TrialverseControllerTest {

  private MockMvc mockMvc;

  @Inject
  private AccountRepository accountRepository;

  @Inject
  private WebApplicationContext webApplicationContext;

  @Inject
  private TriplestoreService triplestoreService;

  @Inject
  private MappingService mappingService;

  private Principal user;

  private Account gert = new Account(3, "gert", "Gert", "van Valkenhoef", "gert@test.com");
  private String namespaceUid = "UID-1";
  private String versionedUuid = "versionedUuid";
  private Integer ownerId = 1;
  private VersionedUuidAndOwner versionedUuidAndOwner = new VersionedUuidAndOwner(versionedUuid, ownerId);

  @Before
  public void setUp() throws URISyntaxException {
    reset(accountRepository, triplestoreService, mappingService);
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    user = mock(Principal.class);
    when(user.getName()).thenReturn("gert");
    when(accountRepository.findAccountByUsername("gert")).thenReturn(gert);
    when(mappingService.getVersionedUuid(namespaceUid)).thenReturn(versionedUuid);
    when(mappingService.getVersionedUuidAndOwner(namespaceUid)).thenReturn(versionedUuidAndOwner);
  }

  @After
  public void cleanUp() throws URISyntaxException {
    verifyNoMoreInteractions(accountRepository, triplestoreService, mappingService);
  }

  @Test
  public void testGetNamespaces() throws Exception {
    String uid1 = "uid 1";
    String uid2 = "uid 2";
    String versionURI = "current";
    int numberOfStudies = 666;
    Namespace namespace1 = new Namespace(uid1, ownerId, "a", "descra", numberOfStudies, versionURI);
    Namespace namespace2 = new Namespace(uid2, ownerId, "b", "descrb", numberOfStudies, versionURI);
    Collection<Namespace> namespaceCollection = Arrays.asList(namespace1, namespace2);
    when(triplestoreService.queryNameSpaces()).thenReturn(namespaceCollection);
    mockMvc.perform(get("/namespaces"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()))
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].name", is("a")));
    verify(triplestoreService).queryNameSpaces();
  }

  @Test
  public void testGetEmptyNamespaces() throws Exception {
    Collection<Namespace> namespaceCollection = Collections.emptyList();
    when(triplestoreService.queryNameSpaces()).thenReturn(namespaceCollection);
    mockMvc.perform(get("/namespaces"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()))
            .andExpect(jsonPath("$", hasSize(0)));
    verify(triplestoreService).queryNameSpaces();
  }


  @Test
  public void testGetNamespaceById() throws Exception {
    int numberOfStudies = 666;
    String versionUid = "current";
    Namespace namespace1 = new Namespace(namespaceUid, ownerId, "a", "descrea", numberOfStudies, versionUid);
    when(triplestoreService.getNamespaceVersioned(versionedUuidAndOwner, versionUid)).thenReturn(namespace1);

    mockMvc.perform(get("/namespaces/UID-1").param("version", versionUid))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()))
            .andExpect(jsonPath("$.name", is("a")));

    verify(triplestoreService).getNamespaceVersioned(versionedUuidAndOwner, versionUid);
    verify(mappingService).getVersionedUuidAndOwner(namespaceUid);
 }

  @Test
  public void testQuerySemanticOutcomes() throws Exception {
    String versionUid = "current";
    SemanticVariable testOutCome = new SemanticVariable("http://test/com", "test label");
    when(triplestoreService.getOutcomes(versionedUuid, versionUid)).thenReturn(Collections.singletonList(testOutCome));

    mockMvc.perform(get("/namespaces/" + namespaceUid + "/outcomes").param("version", versionUid))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()))
            .andExpect(jsonPath("$[0].uri", is(testOutCome.getUri())));

    verify(triplestoreService).getOutcomes(versionedUuid, versionUid);
    verify(mappingService).getVersionedUuid(namespaceUid);
  }

  @Test
  public void testQuerySemanticInterventions() throws Exception {
    String versionUid = "current";
    SemanticIntervention testIntervention = new SemanticIntervention(URI.create("http://test/com"), "test label");
    when(triplestoreService.getInterventions(versionedUuid, versionUid)).thenReturn(Collections.singletonList(testIntervention));

    mockMvc.perform(get("/namespaces/" + namespaceUid + "/interventions").param("version", versionUid))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()))
            .andExpect(jsonPath("$[0].uri", is(testIntervention.getUri().toString())));
    verify(triplestoreService).getInterventions(versionedUuid, versionUid);
    verify(mappingService).getVersionedUuid(namespaceUid);
  }

  @Test
  public void testQuerySemanticStudies() throws Exception {
    String versionUid = "current";
    Study study = new Study("studyUid", "studyGraphUid", "name", "this is a title", Arrays.asList("outcome1", "outcome2"));
    when(triplestoreService.queryStudies(versionedUuid, versionUid)).thenReturn(Collections.singletonList(study));

    mockMvc.perform(get("/namespaces/" + namespaceUid + "/studies").param("version", versionUid))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()))
            .andExpect(jsonPath("$[0].studyUid", is(study.getStudyUid())))
            .andExpect(jsonPath("$[0].name", is(study.getName())))
            .andExpect(jsonPath("$[0].title", is(study.getTitle())));

    verify(triplestoreService).queryStudies(versionedUuid, versionUid);
    verify(mappingService).getVersionedUuid(namespaceUid);
  }

  @Test
  public void testGetTrialDataWithOutcomeAndInterventionsInQuery() throws Throwable {
    List<TrialDataStudy> trialDataStudies = Collections.singletonList(new TrialDataStudy(new URI("abc"), "study name", Collections.emptyList()));
    List<URI> interventionUris = Arrays.asList(URI.create("uri1"), URI.create("uri2"));
    String outcomeUri = "http://someoutcomethisis/12345/abc";
    String versionUid = "current";
    when(triplestoreService.getTrialData(versionedUuid, versionUid, outcomeUri, interventionUris, Collections.emptyList())).thenReturn(trialDataStudies);

    mockMvc.perform(get("/namespaces/" + namespaceUid + "/trialData?interventionUris=uri1&interventionUris=uri2&outcomeUri=" + outcomeUri).principal(user).param("version", versionUid))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()))
            .andExpect(jsonPath("$", notNullValue()));

    verify(triplestoreService).getTrialData(versionedUuid, versionUid, outcomeUri, interventionUris, Collections.emptyList());
    verify(mappingService).getVersionedUuid(namespaceUid);
  }

  @Test
  public void testGetTrialDataWithOutcomeAndNoInterventionsInQuery() throws Throwable {
    List<TrialDataStudy> trialDataStudies = Collections.singletonList(new TrialDataStudy(new URI("abc"), "study name", Collections.emptyList()));
    String outcomeUri = "http://someoutcomethisis/12345/abc";
    String versionUid = "current";
    when(triplestoreService.getTrialData(versionedUuid, versionUid, outcomeUri, Collections.emptyList(), Collections.emptyList())).thenReturn(trialDataStudies);

    mockMvc.perform(get("/namespaces/"+namespaceUid+"/trialData?outcomeUri=" + outcomeUri).param("version", versionUid))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()))
            .andExpect(jsonPath("$", notNullValue()));
    verify(triplestoreService).getTrialData(versionedUuid, versionUid, outcomeUri, Collections.emptyList(), Collections.emptyList());
    verify(mappingService).getVersionedUuid(namespaceUid);
  }

  @Test
  public void testGetStudyWithDetails() throws Exception {
    String studyUid = "studyUid";
    String versionUid = "current";
    when(triplestoreService.getStudydetails(versionedUuid, studyUid)).thenReturn(createStudyWithDetials());

    ResultActions resultActions = mockMvc.perform(get("/namespaces/" + namespaceUid + "/studiesWithDetail/studyUid").param("version", versionUid));
    resultActions
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()))
            .andExpect(jsonPath("$", notNullValue()));

    verify(triplestoreService).getStudydetails(versionedUuid, studyUid);
    verify(mappingService).getVersionedUuid(namespaceUid);
  }

  @Test
  public void testGetStudyGroups() throws Exception {
    String studyUid = "studyUid";
    String versionUid = "current";

    JSONArray result = new JSONArray();
    result.add(createTestResultObject());
    when(triplestoreService.getStudyGroups(versionedUuid, studyUid)).thenReturn(result);

    ResultActions resultActions = mockMvc.perform(get("/namespaces/" + namespaceUid + "/studiesWithDetail/studyUid/groups").param("version", versionUid));
    resultActions
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()))
            .andExpect(jsonPath("$[0].stringObject", is("string value")))
            .andExpect(jsonPath("$[0].numberObject", is(1)))
            .andExpect(jsonPath("$[0].dateObject", is(new DateTime(1980, 9, 10, 12, 0).getMillis())));

    verify(triplestoreService).getStudyGroups(versionedUuid, studyUid);
    verify(mappingService).getVersionedUuid(namespaceUid);
  }

  @Test
  public void testGetStudyEpochs() throws Exception {
    String studyUid = "studyUid";
    String versionUid = "current";

    JSONArray result = new JSONArray();
    result.add(createTestResultObject());
    when(triplestoreService.getStudyEpochs(versionedUuid, studyUid)).thenReturn(result);

    ResultActions resultActions = mockMvc.perform(get("/namespaces/" + namespaceUid + "/studiesWithDetail/studyUid/epochs").param("version", versionUid));
    resultActions.andExpect(status().isOk()); // returns generic result

    verify(triplestoreService).getStudyEpochs(versionedUuid, studyUid);
    verify(mappingService).getVersionedUuid(namespaceUid);
  }

  @Test
  public void testGetStudyTreatmentActivities() throws Exception {
    String studyUid = "studyUid";
    String versionUid = "current";
    List<TreatmentActivity> result = Collections.singletonList(new TreatmentActivity("uir", "type"));
    when(triplestoreService.getStudyTreatmentActivities(versionedUuid, studyUid)).thenReturn(result);

    ResultActions resultActions = mockMvc.perform(get("/namespaces/" + namespaceUid + "/studiesWithDetail/studyUid/treatmentActivities").param("version", versionUid));
    resultActions.andExpect(status().isOk());
    resultActions.andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()));

    verify(triplestoreService).getStudyTreatmentActivities(versionedUuid, studyUid);
    verify(mappingService).getVersionedUuid(namespaceUid);
  }

  @Test
  public void testGetStudyPopulationCharacteristicsData() throws Exception {
    String studyUid = "studyUid";
    String versionUid = "current";

    StudyDataSection studyDataSection = StudyDataSection.BASE_LINE_CHARACTERISTICS;
    List<StudyData> result = Collections.singletonList(new StudyData(studyDataSection, "studyDataTypeUri", "studyDataTypeLabel"));
    when(triplestoreService.getStudyData(versionedUuid, studyUid, studyDataSection)).thenReturn(result);
    ResultActions resultActions = mockMvc.perform(get("/namespaces/" + namespaceUid + "/studiesWithDetail/studyUid/studyData/populationCharacteristics").param("version", versionUid));
    resultActions.andExpect(status().isOk());
    resultActions.andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()));

    verify(triplestoreService).getStudyData(versionedUuid, studyUid, studyDataSection);
    verify(mappingService).getVersionedUuid(namespaceUid);
  }

  @Test
  public void testGetStudyEndpointsData() throws Exception {
    String studyUid = "studyUid";
    String versionUid = "current";

    StudyDataSection studyDataSection = StudyDataSection.ENDPOINTS;
    String versionedUuid = "versionedUuid";
    ResultActions resultActions = mockMvc.perform(get("/namespaces/" + namespaceUid + "/studiesWithDetail/studyUid/studyData/endpoints").param("version", versionUid));
    resultActions.andExpect(status().isOk());
    resultActions.andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()));

    verify(triplestoreService).getStudyData(versionedUuid, studyUid, studyDataSection);
    verify(mappingService).getVersionedUuid(namespaceUid);
  }

  @Test
  public void testGetStudyAdverseEventsData() throws Exception {
    String studyUid = "studyUid";
    String versionUid = "current";

    StudyDataSection studyDataSection = StudyDataSection.ADVERSE_EVENTS;
    List<StudyData> result = Collections.singletonList(new StudyData(studyDataSection, "studyDataTypeUri", "studyDataTypeLabel"));
    when(triplestoreService.getStudyData(versionedUuid, studyUid, studyDataSection)).thenReturn(result);

    ResultActions resultActions = mockMvc.perform(get("/namespaces/"+namespaceUid+"/studiesWithDetail/studyUid/studyData/adverseEvents").param("version", versionUid));
    resultActions.andExpect(status().isOk());
    resultActions.andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()));

    verify(triplestoreService).getStudyData(versionedUuid, studyUid, studyDataSection);
    verify(mappingService).getVersionedUuid(namespaceUid);
  }

  private JSONObject createTestResultObject() {
    JSONObject object = new JSONObject();
    object.put("stringObject", "string value");
    object.put("numberObject", 1);
    object.put("dateObject", new DateTime(1980, 9, 10, 12, 0).toDate());
    return object;
  }

  private StudyWithDetails createStudyWithDetials() {
    return new StudyWithDetails.StudyWithDetailsBuilder()
            .studyUid("studyUid")
            .name("studyName")
            .title("studyTitle")
            .allocation("allocation")
            .blinding("blinding")
            .inclusionCriteria("inclusionCriteria")
            .numberOfStudyCenters(4)
            .pubmedUrls("publicationURL, moreurls")
            .status("status")
            .indication("indication")
            .objectives("objective")
            .investigationalDrugNames("investigationalDrugNames")
            .startDate(new DateTime())
            .endDate(new DateTime())
            .build();
  }

}

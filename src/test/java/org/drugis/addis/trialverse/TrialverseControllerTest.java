package org.drugis.addis.trialverse;


import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.drugis.addis.config.TestConfig;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.trialverse.model.*;
import org.drugis.addis.trialverse.model.emun.StudyDataSection;
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
import java.security.Principal;
import java.util.*;

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

  private Principal user;

  private Account gert = new Account(3, "gert", "Gert", "van Valkenhoef");

  @Before
  public void setUp() {
    reset(accountRepository, triplestoreService);
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    user = mock(Principal.class);
    when(user.getName()).thenReturn("gert");
    when(accountRepository.findAccountByUsername("gert")).thenReturn(gert);
  }

  @After
  public void cleanUp() {
    verifyNoMoreInteractions(accountRepository, triplestoreService);
  }

  @Test
  public void testGetNamespaces() throws Exception {
    String uid1 = "uid 1";
    String uid2 = "uid 2";
    String sourceUrl = "sourceUrl";
    int numberOfStudies = 666;
    Namespace namespace1 = new Namespace(uid1, "a", "descra", numberOfStudies, sourceUrl);
    Namespace namespace2 = new Namespace(uid2, "b", "descrb", numberOfStudies, sourceUrl);
    Collection<Namespace> namespaceCollection = Arrays.asList(namespace1, namespace2);
    when(triplestoreService.queryNameSpaces()).thenReturn(namespaceCollection);
    mockMvc.perform(get("/namespaces"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].name", is("a")));
    verify(triplestoreService).queryNameSpaces();
  }

  @Test
  public void testGetNamespaceById() throws Exception {
    String uid = "UID-1";
    String sourceUrl = "sourceUrl";
    int numberOfStudies = 666;
    Namespace namespace1 = new Namespace(uid, "a", "descrea", numberOfStudies, sourceUrl);
    when(triplestoreService.getNamespace(uid)).thenReturn(namespace1);
    mockMvc.perform(get("/namespaces/UID-1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.name", is("a")));
    verify(triplestoreService).getNamespace(uid);
  }

  @Test
  public void testQuerySemanticOutcomes() throws Exception {
    String namespaceUid = "uid-1";
    SemanticOutcome testOutCome = new SemanticOutcome("http://test/com", "test label");
    when(triplestoreService.getOutcomes(namespaceUid)).thenReturn(Arrays.asList(testOutCome));
    mockMvc.perform(get("/namespaces/" + namespaceUid + "/outcomes"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$[0].uri", is(testOutCome.getUri())));
    verify(triplestoreService).getOutcomes(namespaceUid);
  }

  @Test
  public void testQuerySemanticInterventions() throws Exception {
    String namespaceUid = "abc";
    SemanticIntervention testIntervention = new SemanticIntervention("http://test/com", "test label");
    when(triplestoreService.getInterventions(namespaceUid)).thenReturn(Arrays.asList(testIntervention));
    mockMvc.perform(get("/namespaces/" + namespaceUid + "/interventions"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$[0].uri", is(testIntervention.getUri())));
    verify(triplestoreService).getInterventions(namespaceUid);
  }

  @Test
  public void testQuerySemanticStudies() throws Exception {
    String namespaceUid = "abc";
    Study study = new Study("studyUid", "name", "this is a title");
    when(triplestoreService.queryStudies(namespaceUid)).thenReturn(Arrays.asList(study));
    mockMvc.perform(get("/namespaces/" + namespaceUid + "/studies"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$[0].uid", is(study.getUid())))
            .andExpect(jsonPath("$[0].name", is(study.getName())))
            .andExpect(jsonPath("$[0].title", is(study.getTitle())));
    verify(triplestoreService).queryStudies(namespaceUid);
  }

  @Test
  public void testGetTrialDataWithOutcomeAndInterventionsInQuery() throws Exception {
    Map<TrialDataStudy, List<Pair<Long, String>>> studyInterventions = new HashMap<>();
    List<TrialDataStudy> trialDataStudies = Arrays.asList(new TrialDataStudy("abc", "study name", ListUtils.EMPTY_LIST, ListUtils.EMPTY_LIST));
    Map<String, List<Pair<Long, String>>> studyInterventionKeys = new HashMap<>();
    studyInterventionKeys.put(trialDataStudies.get(0).getStudyUid(), Arrays.asList(Pair.of(101L, "some-sort-of-uri")));
    for (TrialDataStudy trialDataStudy : trialDataStudies) {
      studyInterventions.put(trialDataStudy, studyInterventionKeys.get(trialDataStudy.getStudyUid()));
    }
    String namespaceUid = "namespaceUid";
    List<String> interventionUris = Arrays.asList("uri1", "uri2");
    String outcomeUri = "http://someoutcomethisis/12345/abc";
    when(triplestoreService.getTrialData(namespaceUid, outcomeUri, interventionUris)).thenReturn(trialDataStudies);
    mockMvc.perform(get("/namespaces/namespaceUid/trialData?interventionUris=uri1&interventionUris=uri2&outcomeUri=" + outcomeUri).principal(user))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$", notNullValue()));
    verify(triplestoreService).getTrialData(namespaceUid, outcomeUri, interventionUris);
  }

  @Test
  public void testGetTrialDataWithOutcomeAndNoInterventionsInQuery() throws Exception {
    Map<TrialDataStudy, List<Pair<Long, String>>> studyInterventions = new HashMap<>();
    List<TrialDataStudy> trialDataStudies = Arrays.asList(new TrialDataStudy("abc", "study name", ListUtils.EMPTY_LIST, ListUtils.EMPTY_LIST));
    Map<String, List<Pair<Long, String>>> studyInterventionKeys = new HashMap<>();
    studyInterventionKeys.put(trialDataStudies.get(0).getStudyUid(), Arrays.asList(Pair.of(101L, "some-sort-of-uri")));
    for (TrialDataStudy trialDataStudy : trialDataStudies) {
      studyInterventions.put(trialDataStudy, studyInterventionKeys.get(trialDataStudy.getStudyUid()));
    }
    String namespaceUid = "namespaceUid";
    String outcomeUri = "http://someoutcomethisis/12345/abc";
    when(triplestoreService.getTrialData(namespaceUid, outcomeUri, Collections.EMPTY_LIST)).thenReturn(trialDataStudies);
    mockMvc.perform(get("/namespaces/namespaceUid/trialData?outcomeUri=" + outcomeUri))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$", notNullValue()));
    verify(triplestoreService).getTrialData(namespaceUid, outcomeUri, Collections.EMPTY_LIST);
  }

  @Test
  public void testQueryStudiesWithDetails() throws Exception {
    String namespaceUuid = "namespaceUid";
    List<StudyWithDetails> studyWithDetailsList = Arrays.asList(createStudyWithDetials());
    when(triplestoreService.queryStudydetails(namespaceUuid)).thenReturn(studyWithDetailsList);
    ResultActions resultActions = mockMvc.perform(get("/namespaces/namespaceUid/studiesWithDetail"));
    resultActions
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$[0]", notNullValue()))
            .andExpect(jsonPath("$[0].title", is("studyTitle")))
            .andExpect(jsonPath("$[0].pubmedUrls", is("publicationURL, moreurls")));
    verify(triplestoreService).queryStudydetails(namespaceUuid);

  }

  @Test
  public void testGetStudyWithDetails() throws Exception {
    String namespaceUid = "namespaceUid";
    String studyUid = "studyUid";
    when(triplestoreService.getStudydetails(namespaceUid, studyUid)).thenReturn(createStudyWithDetials());
    ResultActions resultActions = mockMvc.perform(get("/namespaces/namespaceUid/studiesWithDetail/studyUid"));
    resultActions
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$", notNullValue()));
    verify(triplestoreService).getStudydetails(namespaceUid, studyUid);

  }

  @Test
  public void testGetStudyArms() throws Exception {
    String namespaceUid = "namespaceUid";
    String studyUid = "studyUid";
    JSONArray result = new JSONArray();
    result.add(createTestResultObject());
    when(triplestoreService.getStudyArms(namespaceUid, studyUid)).thenReturn(result);
    ResultActions resultActions = mockMvc.perform(get("/namespaces/namespaceUid/studiesWithDetail/studyUid/arms"));
    resultActions
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$[0].stringObject", is("string value")))
            .andExpect(jsonPath("$[0].numberObject", is(1)))
            .andExpect(jsonPath("$[0].dateObject", is(new DateTime(1980, 9, 10, 12, 0).getMillis())));
    verify(triplestoreService).getStudyArms(namespaceUid, studyUid);
  }

  @Test
  public void testGetStudyEpochs() throws Exception {
    String namespaceUid = "namespaceUid";
    String studyUid = "studyUid";
    JSONArray result = new JSONArray();
    result.add(createTestResultObject());
    when(triplestoreService.getStudyEpochs(namespaceUid, studyUid)).thenReturn(result);
    ResultActions resultActions = mockMvc.perform(get("/namespaces/namespaceUid/studiesWithDetail/studyUid/epochs"));
    resultActions.andExpect(status().isOk()); // returns generic result

    verify(triplestoreService).getStudyEpochs(namespaceUid, studyUid);
  }

  @Test
  public void testGetStudyTreatmentActivities() throws Exception {
    String namespaceUid = "namespaceUid";
    String studyUid = "studyUid";
    List<TreatmentActivity> result = Arrays.asList(new TreatmentActivity("uir", "type"));
    when(triplestoreService.getStudyTreatmentActivities(namespaceUid, studyUid)).thenReturn(result);
    ResultActions resultActions = mockMvc.perform(get("/namespaces/namespaceUid/studiesWithDetail/studyUid/treatmentActivities"));
    resultActions.andExpect(status().isOk());
    resultActions.andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8));

    verify(triplestoreService).getStudyTreatmentActivities(namespaceUid, studyUid);
  }

  @Test
  public void testGetStudyPopulationCharacteristicsData() throws Exception {
    String namespaceUid = "namespaceUid";
    String studyUid = "studyUid";
    StudyDataSection studyDataSection = StudyDataSection.BASE_LINE_CHARACTERISTICS;
    List<StudyData> result = Arrays.asList(new StudyData(studyDataSection, "studyDataTypeUri", "studyDataTypeLabel"));
    when(triplestoreService.getStudyData(namespaceUid, studyUid, studyDataSection)).thenReturn(result);
    ResultActions resultActions = mockMvc.perform(get("/namespaces/namespaceUid/studiesWithDetail/studyUid/studyData/populationCharacteristics"));
    resultActions.andExpect(status().isOk());
    resultActions.andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8));

    verify(triplestoreService).getStudyData(namespaceUid, studyUid, studyDataSection);
  }

  @Test
  public void testGetStudyEndpointsData() throws Exception {
    String namespaceUid = "namespaceUid";
    String studyUid = "studyUid";
    StudyDataSection studyDataSection = StudyDataSection.ENDPOINTS;
    List<StudyData> result = Arrays.asList(new StudyData(studyDataSection, "studyDataTypeUri", "studyDataTypeLabel"));
    when(triplestoreService.getStudyData(namespaceUid, studyUid, studyDataSection)).thenReturn(result);
    ResultActions resultActions = mockMvc.perform(get("/namespaces/namespaceUid/studiesWithDetail/studyUid/studyData/endpoints"));
    resultActions.andExpect(status().isOk());
    resultActions.andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8));

    verify(triplestoreService).getStudyData(namespaceUid, studyUid, studyDataSection);
  }

  @Test
  public void testGetStudyAdverseEventsData() throws Exception {
    String namespaceUid = "namespaceUid";
    String studyUid = "studyUid";
    StudyDataSection studyDataSection = StudyDataSection.ADVERSE_EVENTS;
    List<StudyData> result = Arrays.asList(new StudyData(studyDataSection, "studyDataTypeUri", "studyDataTypeLabel"));
    when(triplestoreService.getStudyData(namespaceUid, studyUid, studyDataSection)).thenReturn(result);
    ResultActions resultActions = mockMvc.perform(get("/namespaces/namespaceUid/studiesWithDetail/studyUid/studyData/adverseEvents"));
    resultActions.andExpect(status().isOk());
    resultActions.andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8));

    verify(triplestoreService).getStudyData(namespaceUid, studyUid, studyDataSection);
  }

  private JSONObject createTestResultObject() {
    JSONObject object = new JSONObject();
    object.put("stringObject", "string value");
    object.put("numberObject", new Integer(1));
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

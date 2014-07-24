package org.drugis.addis.trialverse;


import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.SetUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.drugis.addis.config.TestConfig;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.trialverse.model.*;
import org.drugis.addis.trialverse.repository.TrialverseRepository;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.drugis.addis.util.WebConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
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
  private TrialverseRepository trialverseRepository;

  @Inject
  private TriplestoreService triplestoreService;

  private Principal user;

  private Account gert = new Account(3, "gert", "Gert", "van Valkenhoef");

  @Before
  public void setUp() {
    reset(accountRepository, trialverseRepository, triplestoreService);
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    user = mock(Principal.class);
    when(user.getName()).thenReturn("gert");
    when(accountRepository.findAccountByUsername("gert")).thenReturn(gert);
  }

  @After
  public void cleanUp() {
    verifyNoMoreInteractions(accountRepository, trialverseRepository, triplestoreService);
  }

  @Test
  public void testGetNamespaces() throws Exception {
    String uid1 = "uid 1";
    String uid2 = "uid 2";
    Namespace namespace1 = new Namespace(uid1, "a", "descra");
    Namespace namespace2 = new Namespace(uid2, "b", "descrb");
    Collection<Namespace> namespaceCollection = Arrays.asList(namespace1, namespace2);
    when(triplestoreService.queryNameSpaces()).thenReturn(namespaceCollection);
    mockMvc.perform(get("/namespaces").principal(user))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].name", is("a")));
    verify(triplestoreService).queryNameSpaces();
    verify(accountRepository).findAccountByUsername(user.getName());
  }

  @Test
  public void testGetNamespaceById() throws Exception {
    String uid = "UID-1";
    Namespace namespace1 = new Namespace(uid, "a", "descrea");
    when(triplestoreService.getNamespace(uid)).thenReturn(namespace1);
    mockMvc.perform(get("/namespaces/UID-1").principal(user))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.name", is("a")));
    verify(triplestoreService).getNamespace(uid);
    verify(accountRepository).findAccountByUsername(user.getName());
  }

  @Test
  public void testQuerySemanticOutcomes() throws Exception {
    String namespaceUid = "uid-1";
    SemanticOutcome testOutCome = new SemanticOutcome("http://test/com", "test label");
    when(triplestoreService.getOutcomes(namespaceUid)).thenReturn(Arrays.asList(testOutCome));
    mockMvc.perform(get("/namespaces/" + namespaceUid + "/outcomes").principal(user))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$[0].uri", is(testOutCome.getUri())));
    verify(triplestoreService).getOutcomes(namespaceUid);
    verify(accountRepository).findAccountByUsername(user.getName());
  }

  @Test
  public void testQuerySemanticInterventions() throws Exception {
    String namespaceUid = "abc";
    SemanticIntervention testIntervention = new SemanticIntervention("http://test/com", "test label");
    when(triplestoreService.getInterventions(namespaceUid)).thenReturn(Arrays.asList(testIntervention));
    mockMvc.perform(get("/namespaces/" + namespaceUid + "/interventions").principal(user))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$[0].uri", is(testIntervention.getUri())));
    verify(triplestoreService).getInterventions(namespaceUid);
    verify(accountRepository).findAccountByUsername(user.getName());
  }

  @Test
  public void testQuerySemanticStudies() throws Exception {
    String namespaceUid = "abc";
    Study study = new Study("studyUid", "name", "this is a title");
    when(triplestoreService.queryStudies(namespaceUid)).thenReturn(Arrays.asList(study));
    mockMvc.perform(get("/namespaces/" + namespaceUid + "/studies").principal(user))
      .andExpect(status().isOk())
      .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$[0].uid", is(study.getUid())))
      .andExpect(jsonPath("$[0].name", is(study.getName())))
      .andExpect(jsonPath("$[0].title", is(study.getTitle())));
    verify(triplestoreService).queryStudies(namespaceUid);
    verify(accountRepository).findAccountByUsername(user.getName());
  }

  @Test
  public void testUnauthorisedGetSemanticOutcomesFails() throws Exception {
    Principal haxor = mock(Principal.class);
    String userName = "who?";
    when(haxor.getName()).thenReturn(userName);
    mockMvc.perform(get("/namespaces/1/outcomes").principal(haxor))
            .andExpect(redirectedUrl("/error/403"));
    verify(accountRepository).findAccountByUsername(userName);
  }

  @Test
  public void testGetTrialDataWithOutcomeAndInterventionsInQuery() throws Exception {
    Map<TrialDataStudy, List<Pair<Long, String>>> studyInterventions = new HashMap<>();
    List<TrialDataStudy> trialDataStudies = Arrays.asList(new TrialDataStudy("abc", "study name", SetUtils.EMPTY_SET, ListUtils.EMPTY_LIST));
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
    List<TrialDataStudy> trialDataStudies = Arrays.asList(new TrialDataStudy("abc", "study name", SetUtils.EMPTY_SET, ListUtils.EMPTY_LIST));
    Map<String, List<Pair<Long, String>>> studyInterventionKeys = new HashMap<>();
    studyInterventionKeys.put(trialDataStudies.get(0).getStudyUid(), Arrays.asList(Pair.of(101L, "some-sort-of-uri")));
    for (TrialDataStudy trialDataStudy : trialDataStudies) {
      studyInterventions.put(trialDataStudy, studyInterventionKeys.get(trialDataStudy.getStudyUid()));
    }
    String namespaceUid = "namespaceUid";
    String outcomeUri = "http://someoutcomethisis/12345/abc";
    when(triplestoreService.getTrialData(namespaceUid, outcomeUri, Collections.EMPTY_LIST)).thenReturn(trialDataStudies);
    mockMvc.perform(get("/namespaces/namespaceUid/trialData?outcomeUri=" + outcomeUri).principal(user))
      .andExpect(status().isOk())
      .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
      .andExpect(jsonPath("$", notNullValue()));
    verify(triplestoreService).getTrialData(namespaceUid, outcomeUri, Collections.EMPTY_LIST);
  }

}

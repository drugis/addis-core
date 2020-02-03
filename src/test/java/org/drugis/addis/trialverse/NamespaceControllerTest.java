package org.drugis.addis.trialverse;


import net.minidev.json.JSONObject;
import org.drugis.addis.config.TestConfig;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.trialverse.model.*;
import org.drugis.addis.trialverse.model.mapping.TriplestoreUuidAndOwner;
import org.drugis.addis.trialverse.service.MappingService;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.drugis.addis.trialverse.service.impl.ReadValueException;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@WebAppConfiguration
public class NamespaceControllerTest {

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
  private TriplestoreUuidAndOwner triplestoreUuidAndOwner = new TriplestoreUuidAndOwner(versionedUuid, ownerId);
  private final URI versionUri = URI.create("http://versions.com/current");

  @Before
  public void setUp() throws URISyntaxException {
    reset(accountRepository, triplestoreService, mappingService);
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    user = mock(Principal.class);
    when(user.getName()).thenReturn("gert");
    when(accountRepository.findAccountByUsername("gert")).thenReturn(gert);
    when(mappingService.getVersionedUuid(namespaceUid)).thenReturn(versionedUuid);
    when(mappingService.getVersionedUuidAndOwner(namespaceUid)).thenReturn(triplestoreUuidAndOwner);
  }

  @After
  public void cleanUp() throws URISyntaxException {
    verifyNoMoreInteractions(accountRepository, triplestoreService, mappingService);
  }

  @Test
  public void testGetNamespaces() throws Exception {
    String uid1 = "uid 1";
    String uid2 = "uid 2";
    int numberOfStudies = 666;
    URI headVersion = URI.create("http://versions.com/headVersion");
    Namespace namespace1 = new Namespace(uid1, ownerId, "a", "descra", numberOfStudies, versionUri, headVersion);
    Namespace namespace2 = new Namespace(uid2, ownerId, "b", "descrb", numberOfStudies, versionUri, headVersion);
    Collection<Namespace> namespaceCollection = Arrays.asList(namespace1, namespace2);
    when(triplestoreService.queryNameSpaces()).thenReturn(namespaceCollection);
    mockMvc.perform(get("/namespaces"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
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
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(0)));
    verify(triplestoreService).queryNameSpaces();
  }


  @Test
  public void testGetNamespaceById() throws Exception {
    int numberOfStudies = 666;
    URI headVersion = URI.create("http://versions.com/head");
    Namespace namespace1 = new Namespace(namespaceUid, ownerId, "a", "descrea", numberOfStudies, versionUri, headVersion);
    when(triplestoreService.getNamespaceVersioned(triplestoreUuidAndOwner, versionUri)).thenReturn(namespace1);

    mockMvc.perform(get("/namespaces/UID-1").param("version", versionUri.toString()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.name", is("a")));

    verify(triplestoreService).getNamespaceVersioned(triplestoreUuidAndOwner, versionUri);
    verify(mappingService).getVersionedUuidAndOwner(namespaceUid);
 }

  @Test
  public void testQuerySemanticOutcomes() throws Exception, ReadValueException {
    SemanticVariable testOutCome = new SemanticVariable(URI.create("http://test/com"), "test label");
    when(triplestoreService.getOutcomes(versionedUuid, versionUri)).thenReturn(Collections.singletonList(testOutCome));

    mockMvc.perform(get("/namespaces/" + namespaceUid + "/outcomes").param("version", versionUri.toString()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].uri", is(testOutCome.getUri().toString())));

    verify(triplestoreService).getOutcomes(versionedUuid, versionUri);
    verify(mappingService).getVersionedUuid(namespaceUid);
  }

  @Test
  public void testQuerySemanticInterventions() throws Exception {
    SemanticInterventionUriAndName testIntervention = new SemanticInterventionUriAndName(URI.create("http://test/com"), "test label");
    when(triplestoreService.getInterventions(versionedUuid, versionUri)).thenReturn(Collections.singletonList(testIntervention));

    mockMvc.perform(get("/namespaces/" + namespaceUid + "/interventions").param("version", versionUri.toString()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].uri", is(testIntervention.getUri().toString())));
    verify(triplestoreService).getInterventions(versionedUuid, versionUri);
    verify(mappingService).getVersionedUuid(namespaceUid);
  }

  @Test
  public void testQuerySemanticStudies() throws Exception {
    Study study = new Study("studyUuid", "studyGraphUid", "name", "this is a title", Arrays.asList("outcome1", "outcome2"));
    when(triplestoreService.queryStudies(versionedUuid, versionUri)).thenReturn(Collections.singletonList(study));

    mockMvc.perform(get("/namespaces/" + namespaceUid + "/studies").param("version", versionUri.toString()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].studyUid", is(study.getStudyUid())))
            .andExpect(jsonPath("$[0].name", is(study.getName())))
            .andExpect(jsonPath("$[0].title", is(study.getTitle())));

    verify(triplestoreService).queryStudies(versionedUuid, versionUri);
    verify(mappingService).getVersionedUuid(namespaceUid);
  }

  private JSONObject createTestResultObject() {
    JSONObject object = new JSONObject();
    object.put("stringObject", "string value");
    object.put("numberObject", 1);
    object.put("dateObject", new DateTime(1980, 9, 10, 12, 0).toDate());
    return object;
  }

  private StudyWithDetails createStudyWithDetails() {
    return new StudyWithDetails.StudyWithDetailsBuilder()
            .studyUuid("studyUuid")
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

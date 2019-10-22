package org.drugis.addis.interventions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import org.drugis.addis.TestUtils;
import org.drugis.addis.config.TestConfig;
import org.drugis.addis.interventions.controller.command.*;
import org.drugis.addis.interventions.model.*;
import org.drugis.addis.interventions.repository.InterventionRepository;
import org.drugis.addis.interventions.service.InterventionService;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.trialverse.model.SemanticInterventionUriAndName;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;
import java.net.URI;
import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by daan on 3/5/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@WebAppConfiguration
public class InterventionControllerTest {

  private MockMvc mockMvc;

  @Inject
  private AccountRepository accountRepository;

  @Inject
  private InterventionRepository interventionRepository;

  @Inject
  private InterventionService interventionService;

  @Autowired
  private WebApplicationContext webApplicationContext;

  private Principal user;

  private Account gert = new Account(3, "gert", "Gert", "van Valkenhoef", "gert@test.com");


  @Before
  public void setUp() {
    reset(accountRepository, interventionRepository, interventionService);
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    user = mock(Principal.class);
    when(user.getName()).thenReturn("gert");
    when(accountRepository.findAccountByUsername("gert")).thenReturn(gert);
    when(accountRepository.getAccount(user)).thenReturn(gert);
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(accountRepository, interventionRepository, interventionService);
  }

  @Test
  public void testQueryInterventions() throws Exception {

    DoseConstraint constraint = new DoseConstraint(new LowerBoundCommand(LowerBoundType.AT_LEAST, 2d, "mili", "P1D", URI.create("unitConcept"), null), null);
    FixedDoseIntervention intervention = new FixedDoseIntervention(1, 1, "name", "motivation", URI.create("http://semantic.com"), "labelnew", constraint);
    Integer projectId = 1;
    Set<AbstractIntervention> interventions = Sets.newHashSet(intervention);
    when(interventionRepository.query(projectId)).thenReturn(interventions);

    ResultActions result = mockMvc.perform(get("/projects/1/interventions"));
    result
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id", is(intervention.getId())))
            .andExpect(jsonPath("$[0].constraint.lowerBound.value", is(2d)));

    verify(interventionRepository).query(projectId);
  }

  @Test
  public void testQueryCombinationInterventions() throws Exception {

    Integer projectId = 1;
    CombinationIntervention intervention = new CombinationIntervention(1, projectId, "name", "motivation", Sets.newHashSet(1));

    Set<AbstractIntervention> interventions = Sets.newHashSet(intervention);
    when(interventionRepository.query(projectId)).thenReturn(interventions);

    ResultActions result = mockMvc.perform(get("/projects/1/interventions"));
    result
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id", is(intervention.getId())));

    verify(interventionRepository).query(projectId);

  }

  @Test
  public void testGetIntervention() throws Exception {
    SimpleIntervention intervention = new SimpleIntervention(1, 1, "name", "motivation", new SemanticInterventionUriAndName(URI.create("http://semantic.com"), "labelnew"));
    Integer projectId = 1;
    when(interventionRepository.get(projectId)).thenReturn(intervention);
    mockMvc.perform(get("/projects/1/interventions/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id", is(intervention.getId())));
    verify(interventionRepository).get(projectId);
  }

  @Test
  public void updateNameAndDescription() throws Exception {
    Integer projectId = 1;
    Integer interventionId = 2;
    EditInterventionCommand editCommand = new EditInterventionCommand("new name", "new motivation");
    AbstractIntervention updatedIntervention = new SimpleIntervention(interventionId, projectId, editCommand.getName(), editCommand.getMotivation(), URI.create("uri"), "semlabel");
    when(interventionService.updateNameAndMotivation(projectId, interventionId, editCommand.getName(), editCommand.getMotivation())).thenReturn(updatedIntervention);
    String body = TestUtils.createJson(editCommand);

    mockMvc.perform(post("/projects/1/interventions/2").content(body).principal(user).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    verify(accountRepository).getAccount(user);
    verify(interventionService).updateNameAndMotivation(projectId, interventionId, editCommand.getName(), editCommand.getMotivation());
  }

  @Test
  public void testCreateIntervention() throws Exception {
    SimpleIntervention intervention = new SimpleIntervention(1, 1, "name", "motivation", new SemanticInterventionUriAndName(URI.create("http://semantic.com"), "labelnew"));
    AbstractInterventionCommand interventionCommand = new SimpleInterventionCommand(1, "name", "motivation", "http://semantic.com", "labelnew");
    when(interventionRepository.create(gert, interventionCommand)).thenReturn(intervention);
    String body = TestUtils.createJson(interventionCommand);
    mockMvc.perform(post("/projects/1/interventions").content(body).principal(user).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id", notNullValue()));
    verify(accountRepository).findAccountByUsername("gert");
    verify(interventionRepository).create(gert, interventionCommand);
  }


  @Test
  public void testCreateFixedBoundIntervention() throws Exception {
    SimpleIntervention intervention = new SimpleIntervention(1, 1, "name", "motivation", new SemanticInterventionUriAndName(URI.create("http://semantic.com"), "labelnew"));
    LowerBoundType lowerType = LowerBoundType.AT_LEAST;
    UpperBoundType upperType = UpperBoundType.AT_MOST;
    String unit = "mili";
    Double val = 1.1;
    String period = "P2D";
    LowerBoundCommand lower = new LowerBoundCommand(lowerType, val, unit, period, URI.create("unitConcept"), null);
    UpperBoundCommand upper = new UpperBoundCommand(upperType, val, unit, period, URI.create("unitConcept"), null);
    ConstraintCommand fixedDoseConstraintCommand = new ConstraintCommand(lower, upper);
    AbstractInterventionCommand doseRestrictedInterventionCommand = new FixedInterventionCommand(1, "name", "motivation", "http://semantic.com", "labelnew", fixedDoseConstraintCommand);
    when(interventionRepository.create(gert, doseRestrictedInterventionCommand)).thenReturn(intervention);
    String body = TestUtils.createJson(doseRestrictedInterventionCommand);
    mockMvc.perform(post("/projects/1/interventions").content(body).principal(user).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id", notNullValue()));
    verify(accountRepository).findAccountByUsername(gert.getUsername());
    verify(interventionRepository).create(gert, doseRestrictedInterventionCommand);
  }

  @Test
  public void createCombinationIntervention() throws Exception {
    Set<Integer> interventions = new HashSet<>();
    interventions.add(1);
    CombinationIntervention combinationIntervention = new CombinationIntervention(1, 1, "name", "motivation", interventions);
    Set<Integer> interventionsIds = Sets.newHashSet(1);
    AbstractInterventionCommand combinationInterventionCommand = new CombinationInterventionCommand(1, "name", "motivation", interventionsIds);
    when(interventionRepository.create(gert, combinationInterventionCommand)).thenReturn(combinationIntervention);
    String body = TestUtils.createJson(combinationInterventionCommand);
    mockMvc.perform(post("/projects/1/interventions").content(body).principal(user).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id", notNullValue()));
    verify(accountRepository).findAccountByUsername(gert.getUsername());
    verify(interventionRepository).create(gert, combinationInterventionCommand);
  }

  @Test
  public void testCreateTitratedDoseIntervention() throws Exception {
    String body = "{\n" +
            "  \"type\": \"titrated\",\n" +
            "  \"titratedDoseMinConstraint\": {\n" +
            "    \"lowerBound\": {\n" +
            "      \"type\": \"AT_LEAST\",\n" +
            "      \"unitName\": \"milligram\",\n" +
            "      \"unitPeriod\": \"P1D\",\n" +
            "      \"unitConcept\": \"unitConcept\",\n" +
            "      \"value\": 30\n" +
            "    }\n" +
            "  },\n" +
            "  \"titratedDoseMaxConstraint\": {\n" +
            "    \"upperBound\": {\n" +
            "      \"type\": \"LESS_THAN\",\n" +
            "      \"unitName\": \"milligram\",\n" +
            "      \"unitPeriod\": \"P1D\",\n" +
            "      \"unitConcept\": \"unitConcept\",\n" +
            "      \"value\": 50\n" +
            "    }\n" +
            "  },\n" +
            "  \"name\": \"Bupropion\",\n" +
            "  \"projectId\": 13,\n" +
            "  \"semanticInterventionLabel\": \"Bupropion\",\n" +
            "  \"semanticInterventionUri\": \"234-aga-34\"\n" +
            "}\n";
    SimpleIntervention intervention = new SimpleIntervention(1, 1, "name", "motivation", new SemanticInterventionUriAndName(URI.create("http://semantic.com"), "labelnew"));
    ObjectMapper mapper = new ObjectMapper();
    AbstractInterventionCommand doseRestrictedInterventionCommand = mapper.readValue(body, AbstractInterventionCommand.class);
    when(interventionRepository.create(gert, doseRestrictedInterventionCommand)).thenReturn(intervention);
    mockMvc.perform(post("/projects/1/interventions").content(body).principal(user).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id", notNullValue()));
    verify(accountRepository).findAccountByUsername(gert.getUsername());
    verify(interventionRepository).create(gert, doseRestrictedInterventionCommand);
  }

  @Test
  public void testCreateBothDoseIntervention() throws Exception {
    String body = "{\n" +
            "  \"type\": \"both\",\n" +
            "  \"bothDoseTypesMinConstraint\": {\n" +
            "    \"lowerBound\": {\n" +
            "      \"type\": \"AT_LEAST\",\n" +
            "      \"unitName\": \"milligram\",\n" +
            "      \"unitPeriod\": \"P1D\",\n" +
            "      \"unitConcept\": \"unitConcept\",\n" +
            "      \"value\": 30\n" +
            "    }\n" +
            "  },\n" +
            "  \"name\": \"Bupropion\",\n" +
            "  \"projectId\": 13,\n" +
            "  \"semanticInterventionLabel\": \"Bupropion\",\n" +
            "  \"semanticInterventionUri\": \"234-aga-34\"\n" +
            "}\n";
    SimpleIntervention intervention = new SimpleIntervention(1, 1, "name", "motivation", new SemanticInterventionUriAndName(URI.create("http://semantic.com"), "labelnew"));
    ObjectMapper mapper = new ObjectMapper();
    AbstractInterventionCommand doseRestrictedInterventionCommand = mapper.readValue(body, AbstractInterventionCommand.class);
    when(interventionRepository.create(gert, doseRestrictedInterventionCommand)).thenReturn(intervention);
    mockMvc.perform(post("/projects/1/interventions").content(body).principal(user).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id", notNullValue()));
    verify(accountRepository).findAccountByUsername(gert.getUsername());
    verify(interventionRepository).create(gert, doseRestrictedInterventionCommand);
  }

  @Test
  public void testDeleteIntervention() throws Exception {
    mockMvc.perform(delete("/projects/1/interventions/2").principal(user))
            .andExpect(status().isOk());
    verify(accountRepository).findAccountByUsername(gert.getUsername());
    verify(interventionService).delete(1, 2);
  }

  @Test
  public void testSetConversionMultiplier() throws Exception {
    String body = "{\"multipliers\": [{\"unitName\": \"milligram\", " +
            "\"unitConcept\": \"http://conceptURI.com\", \"conversionMultiplier\": 0.001}]}";
    mockMvc.perform(post("/projects/1/interventions/2/setConversionMultiplier")
            .content(body)
            .principal(user)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    InterventionMultiplierCommand multiplier = new InterventionMultiplierCommand("milligram",
            URI.create("http://conceptURI.com"), 0.001);
    List<InterventionMultiplierCommand> multipliers = Collections.singletonList(multiplier);
    SetMultipliersCommand command = new SetMultipliersCommand(multipliers);
    verify(accountRepository).findAccountByUsername(gert.getUsername());
    verify(interventionService).setMultipliers(2, command);
  }

}

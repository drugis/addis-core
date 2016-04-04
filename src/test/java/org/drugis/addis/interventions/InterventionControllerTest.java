package org.drugis.addis.interventions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.drugis.addis.TestUtils;
import org.drugis.addis.config.TestConfig;
import org.drugis.addis.interventions.model.BoundCommand;
import org.drugis.addis.interventions.model.ConstraintCommand;
import org.drugis.addis.interventions.model.Intervention;
import org.drugis.addis.interventions.model.InterventionCommand;
import org.drugis.addis.interventions.repository.InterventionRepository;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.trialverse.model.SemanticIntervention;
import org.drugis.addis.util.WebConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

  @Autowired
  private WebApplicationContext webApplicationContext;

  private Principal user;

  private Account gert = new Account(3, "gert", "Gert", "van Valkenhoef", "gert@test.com");


  @Before
  public void setUp() {
    reset(accountRepository);
    reset(interventionRepository);
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    user = mock(Principal.class);
    when(user.getName()).thenReturn("gert");
    when(accountRepository.findAccountByUsername("gert")).thenReturn(gert);
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(accountRepository, interventionRepository);
  }

  @Test
  public void testQueryInterventions() throws Exception {
    Intervention intervention = new Intervention(1, "name", "motivation", new SemanticIntervention("http://semantic.com", "labelnew"));
    Integer projectId = 1;
    List<Intervention> interventions = Arrays.asList(intervention);
    when(interventionRepository.query(projectId)).thenReturn(interventions);

    mockMvc.perform(get("/projects/1/interventions").principal(user))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id", is(intervention.getId())));

    verify(interventionRepository).query(projectId);
    verify(accountRepository).findAccountByUsername("gert");
  }

  @Test
  public void testUnauthorisedAccessFails() throws Exception {
    when(accountRepository.findAccountByUsername("gert")).thenReturn(null);
    mockMvc.perform(get("/projects/1/interventions").principal(user))
            .andExpect(status().isForbidden());
    verify(accountRepository).findAccountByUsername("gert");
  }

  @Test
  public void testGetIntervention() throws Exception {
    Intervention intervention = new Intervention(1, 1, "name", "motivation", new SemanticIntervention("http://semantic.com", "labelnew"));
    Integer projectId = 1;
    when(interventionRepository.get(projectId, intervention.getId())).thenReturn(intervention);
    mockMvc.perform(get("/projects/1/interventions/1").principal(user))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()))
            .andExpect(jsonPath("$.id", is(intervention.getId())));
    verify(accountRepository).findAccountByUsername("gert");
    verify(interventionRepository).get(projectId, intervention.getId());
  }

  @Test
  public void testCreateIntervention() throws Exception {
    Intervention intervention = new Intervention(1, 1, "name", "motivation", new SemanticIntervention("http://semantic.com", "labelnew"));
    InterventionCommand interventionCommand = new InterventionCommand(1, "name", "motivation", new SemanticIntervention("http://semantic.com", "labelnew"));
    when(interventionRepository.create(gert, interventionCommand)).thenReturn(intervention);
    String body = TestUtils.createJson(interventionCommand);
    mockMvc.perform(post("/projects/1/interventions").content(body).principal(user).contentType(WebConstants.getApplicationJsonUtf8Value()))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()))
            .andExpect(jsonPath("$.id", notNullValue()));
    verify(accountRepository).findAccountByUsername("gert");
    verify(interventionRepository).create(gert, interventionCommand);
  }


  @Test
  public void testCreateFixedBoundIntervention() throws Exception {
    Intervention intervention = new Intervention(1, 1, "name", "motivation", new SemanticIntervention("http://semantic.com", "labelnew"));
    ConstraintCommand fixedDoseConstraintCommand = null;
    InterventionCommand interventionCommand = new InterventionCommand(1, "name", "motivation", new SemanticIntervention("http://semantic.com", "labelnew"), fixedDoseConstraintCommand, null, null, null, null);
    when(interventionRepository.create(gert, interventionCommand)).thenReturn(intervention);
    String body = TestUtils.createJson(interventionCommand);
    mockMvc.perform(post("/projects/1/interventions").content(body).principal(user).contentType(WebConstants.getApplicationJsonUtf8Value()))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()))
            .andExpect(jsonPath("$.id", notNullValue()));
    verify(accountRepository).findAccountByUsername(gert.getUsername());
    verify(interventionRepository).create(gert, interventionCommand);
  }

  @Test
  public void testCreateTitratedDoseIntervention() throws Exception {
    String body = "{\n" +
            "  \"doseType\": \"titrated\",\n" +
            "  \"titratedDoseMinConstraint\": {\n" +
            "    \"lowerBound\": {\n" +
            "      \"type\": \"atLeast\",\n" +
            "      \"unit\": \"milligram\",\n" +
            "      \"value\": 30\n" +
            "    }\n" +
            "  },\n" +
            "  \"titratedDoseMaxConstraint\": {\n" +
            "    \"upperBound\": {\n" +
            "      \"type\": \"lessThan\",\n" +
            "      \"unit\": \"milligram\",\n" +
            "      \"value\": 50\n" +
            "    }\n" +
            "  },\n" +
            "  \"semanticIntervention\": {\n" +
            "    \"uri\": \"6ed4cd55-3ebc-497c-b720-388456cca75e\",\n" +
            "    \"label\": \"Bupropion\"\n" +
            "  },\n" +
            "  \"name\": \"Bupropion\",\n" +
            "  \"projectId\": 13,\n" +
            "  \"semanticInterventionLabel\": \"Bupropion\"\n" +
            "}\n";
    Intervention intervention = new Intervention(1, 1, "name", "motivation", new SemanticIntervention("http://semantic.com", "labelnew"));
    ObjectMapper mapper = new ObjectMapper();
    InterventionCommand interventionCommand = mapper.readValue(body, InterventionCommand.class);
    when(interventionRepository.create(gert, interventionCommand)).thenReturn(intervention);
    mockMvc.perform(post("/projects/1/interventions").content(body).principal(user).contentType(WebConstants.getApplicationJsonUtf8Value()))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()))
            .andExpect(jsonPath("$.id", notNullValue()));
    verify(accountRepository).findAccountByUsername(gert.getUsername());
    verify(interventionRepository).create(gert, interventionCommand);
  }

}

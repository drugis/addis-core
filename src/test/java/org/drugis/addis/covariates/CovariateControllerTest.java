package org.drugis.addis.covariates;

import org.apache.http.entity.ContentType;
import org.drugis.addis.TestUtils;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.trialverse.model.emun.CovariateOption;
import org.drugis.addis.trialverse.model.emun.CovariateOptionType;
import org.drugis.addis.util.WebConstants;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by connor on 12/2/15.
 */
@Configuration
@EnableWebMvc
public class CovariateControllerTest {
  private MockMvc mockMvc;
  private Principal user = mock(Principal.class);
  private Account gert = new Account(3, "gert", "Gert", "van Valkenhoef", "gert@test.com");

  @Mock
  private ProjectService projectService;

  @Mock
  private AccountRepository accountRepository;

  @Mock
  private CovariateRepository covariateRepository;

  @Mock
  private CovariateService covariateService;

  @InjectMocks
  private CovariateController covariateController;


  @Before
  public void setUp() {
    when(user.getName()).thenReturn("gert");
    initMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(covariateController).build();
  }

  @Test
  public void getCovariatesForProjectTest() throws Exception {
    Integer projectId = 1;
    Covariate covariate1 = new Covariate(projectId, "name", "motivation", CovariateOption.MULTI_CENTER_STUDY.toString(), CovariateOptionType.STUDY_CHARACTERISTIC);
    Covariate covariate2 = new Covariate(projectId, "other name", "motivation", CovariateOption.ALLOCATION_RANDOMIZED.toString(), CovariateOptionType.STUDY_CHARACTERISTIC);
    Collection<Covariate> covariates = Arrays.asList(covariate1, covariate2);
    when(covariateRepository.findByProject(projectId)).thenReturn(covariates);
    ResultActions result = mockMvc.perform(get("/projects/1/covariates"));
    result.andExpect(status().isOk());
    result.andExpect(content().contentType(MediaType.APPLICATION_JSON));
    result.andExpect(jsonPath("$", hasSize(2)));
    result.andExpect(jsonPath("$[0].name", is("name")));
  }

  @Test
  public void addCovariateStudyCharacteristicTest() throws Exception {
    Integer projectId = 1;
    AddCovariateCommand addCovariateCommand = new AddCovariateCommand(CovariateOption.ALLOCATION_RANDOMIZED.toString(),
            "my test covariate", "my motivation", CovariateOptionType.STUDY_CHARACTERISTIC);
    String body = TestUtils.createJson(addCovariateCommand);
    when(accountRepository.findAccountByUsername(user.getName())).thenReturn(gert);
    Covariate covariate = new Covariate(projectId, "name", "motivation", CovariateOption.MULTI_CENTER_STUDY.toString(), CovariateOptionType.STUDY_CHARACTERISTIC);
    when(covariateRepository.createForProject(projectId, addCovariateCommand.getCovariateDefinitionKey(),
            addCovariateCommand.getName(), addCovariateCommand.getMotivation(), addCovariateCommand.getType()))
            .thenReturn(covariate);
    ResultActions resultActions = mockMvc.perform(post("/projects/1/covariates")
            .content(body)
            .principal(user)
            .contentType(MediaType.APPLICATION_JSON));
    resultActions.andExpect(status().isCreated());
    resultActions.andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()));
    resultActions.andExpect(jsonPath("$.name", is("name")));
    resultActions.andExpect(jsonPath("$.definitionKey", is(CovariateOption.MULTI_CENTER_STUDY.toString())));
    verify(accountRepository).findAccountByUsername(gert.getUsername());
    verify(projectService).checkProjectExistsAndModifiable(gert, projectId);
    verify(covariateRepository).createForProject(projectId, addCovariateCommand.getCovariateDefinitionKey(),
            addCovariateCommand.getName(), addCovariateCommand.getMotivation(), CovariateOptionType.STUDY_CHARACTERISTIC);
  }

  @Test
  public void addCovariatePopulationCharacteristicTest() throws Exception {
    Integer projectId = 1;
    String populationCharacteristicUri = "eee3f-2353-234";
    AddCovariateCommand addCovariateCommand = new AddCovariateCommand(populationCharacteristicUri,
            "my test covariate", "my motivation", CovariateOptionType.POPULATION_CHARACTERISTIC);
    String body = TestUtils.createJson(addCovariateCommand);
    when(accountRepository.findAccountByUsername(user.getName())).thenReturn(gert);
    Covariate covariate = new Covariate(projectId, "name", "motivation", populationCharacteristicUri, CovariateOptionType.POPULATION_CHARACTERISTIC);
    when(covariateRepository.createForProject(projectId, addCovariateCommand.getCovariateDefinitionKey(),
            addCovariateCommand.getName(), addCovariateCommand.getMotivation(), addCovariateCommand.getType()))
            .thenReturn(covariate);
    ResultActions resultActions = mockMvc.perform(post("/projects/1/covariates")
            .content(body)
            .principal(user)
            .contentType(MediaType.APPLICATION_JSON));
    resultActions.andExpect(status().isCreated());
    resultActions.andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()));
    resultActions.andExpect(jsonPath("$.name", is("name")));
    resultActions.andExpect(jsonPath("$.definitionKey", is(populationCharacteristicUri)));
    verify(accountRepository).findAccountByUsername(gert.getUsername());
    verify(projectService).checkProjectExistsAndModifiable(gert, projectId);
    verify(covariateRepository).createForProject(projectId, addCovariateCommand.getCovariateDefinitionKey(),
            addCovariateCommand.getName(), addCovariateCommand.getMotivation(), CovariateOptionType.POPULATION_CHARACTERISTIC);
  }

  @Test
  public void testDeleteCovariate() throws Exception {
    when(accountRepository.findAccountByUsername(user.getName())).thenReturn(gert);
    mockMvc.perform(delete("/projects/1/covariates/37").principal(user)).andExpect(status().isOk());
    verify(covariateService).delete(gert, 1, 37);
  }
}

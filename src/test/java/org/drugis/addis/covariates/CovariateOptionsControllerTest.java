package org.drugis.addis.covariates;

import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.outcomes.repository.OutcomeRepository;
import org.drugis.addis.projects.Project;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.trialverse.controller.CovariateOptionsController;
import org.drugis.addis.trialverse.model.SemanticVariable;
import org.drugis.addis.trialverse.model.emun.CovariateOption;
import org.drugis.addis.trialverse.service.MappingService;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.drugis.addis.trialverse.service.impl.ReadValueException;
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

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by connor on 12/1/15.
 */
@Configuration
@EnableWebMvc
public class CovariateOptionsControllerTest {

  private MockMvc mockMvc;

  @Mock
  OutcomeRepository outcomeRepository;

  @Mock
  ProjectRepository projectRepository;

  @Mock
  TriplestoreService triplestoreService;

  @Mock
  MappingService mappingService;

  @InjectMocks
  private CovariateOptionsController covariateOptionsController;


  @Before
  public void setUp() {
    initMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(covariateOptionsController).build();
  }

  @Test
  public void getCovariateOptions() throws Exception {
    ResultActions result = mockMvc.perform(get("/covariate-options"));
    result.andExpect(status().isOk());
    result.andExpect(content().contentType(MediaType.APPLICATION_JSON));
    result.andExpect(jsonPath("$", hasSize(5)));
    result.andExpect(jsonPath("$[0].key", is(CovariateOption.ALLOCATION_RANDOMIZED.toString())));
    result.andExpect(jsonPath("$[0].label", is(CovariateOption.ALLOCATION_RANDOMIZED.getLabel())));
    result.andExpect(jsonPath("$[0].typeKey", is(CovariateOption.ALLOCATION_RANDOMIZED.getType().toString())));
    result.andExpect(jsonPath("$[0].typeLabel", is(CovariateOption.ALLOCATION_RANDOMIZED.getType().getLabel())));
  }

  @Test
  public void getCovariateOptionsForProject() throws Exception, ReadValueException {
    Integer projectId = 1;
    String tvUuid = "tvUuid";
    URI version = URI.create("http://version.com");
    Project project = new Project(null, null, null, "uuid", version);
    SemanticVariable semPopChar = new SemanticVariable(URI.create("uri1"), "label1");
    SemanticVariable semNonPopCharVar = new SemanticVariable(URI.create("uri2"), "label2");
    Integer direction = 1;
    Outcome popChar = new Outcome(projectId, "name1", direction, "", semPopChar);
    Outcome otherTypeOutcome = new Outcome(projectId, "name2", direction, "", semNonPopCharVar);

    List<SemanticVariable> semanticOutcomes = Collections.singletonList(semPopChar);
    Collection<Outcome> outcomes = Arrays.asList(popChar, otherTypeOutcome);

    when(projectRepository.get(projectId)).thenReturn(project);
    when(mappingService.getVersionedUuid(project.getNamespaceUid())).thenReturn(tvUuid);
    when(triplestoreService.getPopulationCharacteristics(tvUuid, version)).thenReturn(semanticOutcomes);
    when(outcomeRepository.query(projectId)).thenReturn(outcomes);

    ResultActions result = mockMvc.perform(get("/projects/" + projectId + "/covariate-options"));

    result.andExpect(status().isOk());
    result.andExpect(content().contentType(MediaType.APPLICATION_JSON));
    result.andExpect(jsonPath("$", hasSize(6))); // 1 = number of popchar outcomes in project
    result.andExpect(jsonPath("$[5].label", is(semPopChar.getLabel())));
  }
}

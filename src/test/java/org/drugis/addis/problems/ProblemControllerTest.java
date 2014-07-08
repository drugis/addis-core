package org.drugis.addis.problems;

import org.drugis.addis.config.TestConfig;
import org.drugis.addis.problems.model.*;
import org.drugis.addis.problems.service.ProblemService;
import org.drugis.addis.problems.service.model.*;
import org.drugis.addis.util.WebConstants;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by daan on 3/21/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@WebAppConfiguration
public class ProblemControllerTest {

  private MockMvc mockMvc;

  @Inject
  ProblemService problemService;

  @Inject
  private WebApplicationContext webApplicationContext;

  @Before
  public void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
  }

  @Test
  public void testGetSingleStudybenefitRiskProblem() throws Exception {
    RatePerformance ratePerformance = new RatePerformance(new RatePerformanceParameters(10L, 50L));
    String alternativeUri1 = "Alt uri 1";
    String alternativeUri2 = "Alt uri 2";
    String criterionUri1 = "Crit uri 1";
    String criterionUri2 = "Crit uri 2";
    AbstractMeasurementEntry rateMeasurementEntry = new RateMeasurementEntry(alternativeUri1, criterionUri1, ratePerformance);
    ContinuousPerformance continuousPerformance = new ContinuousPerformance(new ContinuousPerformanceParameters(7.5, 2.1));
    AbstractMeasurementEntry continuousMeasurementEntry = new ContinuousMeasurementEntry(alternativeUri2, criterionUri2, continuousPerformance);
    List<AbstractMeasurementEntry> performanceTable = Arrays.asList(rateMeasurementEntry, continuousMeasurementEntry);
    SingleStudyBenefitRiskProblem problem = new SingleStudyBenefitRiskProblem("testProblem", new HashMap<String, AlternativeEntry>(), new HashMap<String, CriterionEntry>(), performanceTable);
    Integer projectId = 1;
    Integer analysisId = 1;
    when(problemService.getProblem(projectId, analysisId)).thenReturn(problem);
    mockMvc.perform(get("/projects/1/analyses/1/problem"))
      .andExpect(status().isOk())
      .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
      .andExpect(jsonPath("$", notNullValue()))
      .andExpect(jsonPath("$.title", equalTo(problem.getTitle())))
      .andExpect(jsonPath("$.performanceTable", hasSize(2)))
      .andExpect(jsonPath("$.performanceTable[0].performance.type", is(RatePerformance.DBETA)))
      .andExpect(jsonPath("$.performanceTable[1].performance.type", is(ContinuousPerformance.DNORM)));
    verify(problemService).getProblem(projectId, analysisId);
  }

  @Test
  public void testGetNetworkMetaAnalysisProblem() throws  Exception {
    AbstractNetworkMetaAnalysisProblemEntry entry1 = new RateNetworkMetaAnalysisProblemEntry("study", "treatment1", 10L, 5L);
    AbstractNetworkMetaAnalysisProblemEntry entry2 = new RateNetworkMetaAnalysisProblemEntry("study", "treatment2", 20L, 7L);
    List<AbstractNetworkMetaAnalysisProblemEntry> entries = Arrays.asList(entry1, entry2);
    NetworkMetaAnalysisProblem networkMetaAnalysisProblem = new NetworkMetaAnalysisProblem(entries);
     Integer projectId = 1;
    Integer analysisId = 2;
    when(problemService.getProblem(projectId, analysisId)).thenReturn(networkMetaAnalysisProblem);
    mockMvc.perform(get("/projects/1/analyses/2/problem"))
      .andExpect(status().isOk())
      .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
      .andExpect(jsonPath("$", notNullValue()))
      .andExpect(jsonPath("$.entries", hasSize(2)))
      .andExpect((jsonPath("$.entries[0].treatment", equalTo(entry1.getTreatment()))))
      .andExpect((jsonPath("$.entries[0].responders", is(((RateNetworkMetaAnalysisProblemEntry)entry1).getResponders().intValue()))));
    verify(problemService).getProblem(projectId, analysisId);
  }

}

package org.drugis.addis.problems;

import org.drugis.addis.config.TestConfig;
import org.drugis.addis.problems.model.AlternativeEntry;
import org.drugis.addis.problems.model.CriterionEntry;
import org.drugis.addis.problems.model.Problem;
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
  public void testGetProblem() throws Exception {
    RatePerformance ratePerformance = new RatePerformance(new RatePerformanceParameters(10L, 50L));
    AbstractMeasurementEntry rateMeasurementEntry = new RateMeasurementEntry("alternative 1", "criterion 1", ratePerformance);
    ContinuousPerformance continuousPerformance = new ContinuousPerformance(new ContinuousPerformanceParameters(7.5, 2.1));
    AbstractMeasurementEntry continuousMeasurementEntry = new ContinuousMeasurementEntry("alternative 2", "criterion 2", continuousPerformance);
    List<AbstractMeasurementEntry> performanceTable = Arrays.asList(rateMeasurementEntry, continuousMeasurementEntry);
    Problem problem = new Problem("testProblem", new HashMap<String, AlternativeEntry>(), new HashMap<String, CriterionEntry>(), performanceTable);
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

}

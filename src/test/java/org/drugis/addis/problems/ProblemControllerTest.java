package org.drugis.addis.problems;

import net.minidev.json.JSONObject;
import org.drugis.addis.config.TestConfig;
import org.drugis.addis.exception.ProblemCreationException;
import org.drugis.addis.interventions.service.impl.InvalidTypeForDoseCheckException;
import org.drugis.addis.problems.model.*;
import org.drugis.addis.problems.service.ProblemService;
import org.drugis.addis.problems.service.model.*;
import org.drugis.addis.trialverse.service.impl.ReadValueException;
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
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by daan on 3/21/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@WebAppConfiguration
public class ProblemControllerTest {

  @Inject
  ProblemService problemService;
  private MockMvc mockMvc;
  @Inject
  private WebApplicationContext webApplicationContext;

  @Before
  public void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    reset(problemService);
  }

  @Test
  public void testGetSingleStudyBenefitRiskProblem() throws Exception, ReadValueException, InvalidTypeForDoseCheckException, ProblemCreationException {
    RatePerformance ratePerformance = new RatePerformance(new RatePerformanceParameters(10, 50));
    Integer alternative1 = 1;
    Integer alternative2 = 2;
    AbstractMeasurementEntry rateMeasurementEntry = new RateMeasurementEntry(alternative1, "Crituri1", ratePerformance);
    ContinuousPerformance continuousPerformance = new ContinuousPerformance(new ContinuousPerformanceParameters(7.5, 2.1));
    AbstractMeasurementEntry continuousMeasurementEntry = new ContinuousMeasurementEntry(alternative2, "Crituri2", continuousPerformance);
    List<AbstractMeasurementEntry> performanceTable = Arrays.asList(rateMeasurementEntry, continuousMeasurementEntry);
    SingleStudyBenefitRiskProblem problem = new SingleStudyBenefitRiskProblem(new HashMap<>(), new HashMap<>(), performanceTable);
    Integer projectId = 1;
    Integer analysisId = 1;
    when(problemService.getProblem(projectId, analysisId, )).thenReturn(problem);
    mockMvc.perform(get("/projects/1/analyses/1/problem"))
      .andExpect(status().isOk())
      .andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()))
      .andExpect(jsonPath("$", notNullValue()))
      .andExpect(jsonPath("$.performanceTable", hasSize(2)))
      .andExpect(jsonPath("$.performanceTable[0].performance.type", is(RatePerformance.DBETA)))
      .andExpect(jsonPath("$.performanceTable[1].performance.type", is(ContinuousPerformance.DNORM)));
    verify(problemService).getProblem(projectId, analysisId, );
  }

  @Test
  public void testGetNetworkMetaAnalysisProblem() throws Exception, ReadValueException, InvalidTypeForDoseCheckException, ProblemCreationException {
    int treatmentId1 = 1;
    int treatmentId2 = 2;
    AbstractNetworkMetaAnalysisProblemEntry entry1 = new RateNetworkMetaAnalysisProblemEntry("study", treatmentId1, 10, 5);
    AbstractNetworkMetaAnalysisProblemEntry entry2 = new RateNetworkMetaAnalysisProblemEntry("study", treatmentId2, 20, 7);
    List<AbstractNetworkMetaAnalysisProblemEntry> entries = Arrays.asList(entry1, entry2);
    List<TreatmentEntry> treatments = Arrays.asList(new TreatmentEntry(treatmentId1, "treatment 1 name"), new TreatmentEntry(treatmentId2, "treatment 2 name"));
    Map<String, Map<String, Double>> studyCovariates = new HashMap<>();
    NetworkMetaAnalysisProblem networkMetaAnalysisProblem = new NetworkMetaAnalysisProblem(entries, treatments, studyCovariates);
    Integer projectId = 1;
    Integer analysisId = 2;
    when(problemService.getProblem(projectId, analysisId, )).thenReturn(networkMetaAnalysisProblem);
    mockMvc.perform(get("/projects/1/analyses/2/problem"))
      .andExpect(status().isOk())
      .andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()))
      .andExpect(jsonPath("$", notNullValue()))
      .andExpect(jsonPath("$.entries", hasSize(2)))
      .andExpect((jsonPath("$.entries[0].treatment", equalTo(entry1.getTreatment()))))
      .andExpect((jsonPath("$.entries[0].responders", is(((RateNetworkMetaAnalysisProblemEntry) entry1).getResponders()))))
            .andExpect((jsonPath("$.treatments[0].id", equalTo(treatmentId1))))
            .andExpect((jsonPath("$.studyLevelCovariates", equalTo(new JSONObject()))));
    verify(problemService).getProblem(projectId, analysisId, );
  }

  @Test
  public void testGetNetworkMetaAnalysisNoCovariates() throws Exception, ReadValueException, InvalidTypeForDoseCheckException, ProblemCreationException {
    int treatmentId1 = 1;
    int treatmentId2 = 2;
    AbstractNetworkMetaAnalysisProblemEntry entry1 = new RateNetworkMetaAnalysisProblemEntry("study", treatmentId1, 10, 5);
    AbstractNetworkMetaAnalysisProblemEntry entry2 = new RateNetworkMetaAnalysisProblemEntry("study", treatmentId2, 20, 7);
    List<AbstractNetworkMetaAnalysisProblemEntry> entries = Arrays.asList(entry1, entry2);
    List<TreatmentEntry> treatments = Arrays.asList(new TreatmentEntry(treatmentId1, "treatment 1 name"), new TreatmentEntry(treatmentId2, "treatment 2 name"));

    NetworkMetaAnalysisProblem networkMetaAnalysisProblem = new NetworkMetaAnalysisProblem(entries, treatments, null);
    Integer projectId = 1;
    Integer analysisId = 2;
    when(problemService.getProblem(projectId, analysisId, )).thenReturn(networkMetaAnalysisProblem);
    mockMvc.perform(get("/projects/1/analyses/2/problem"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()))
            .andExpect(jsonPath("$", notNullValue()))
            .andExpect(jsonPath("$.entries", hasSize(2)))
            .andExpect(jsonPath("$.entries[0].treatment", equalTo(entry1.getTreatment())))
            .andExpect(jsonPath("$.entries[0].responders", is(((RateNetworkMetaAnalysisProblemEntry) entry1).getResponders())))
            .andExpect(jsonPath("$.treatments[0].id", equalTo(treatmentId1)))
            .andExpect(jsonPath("$.studyLevelCovariates").doesNotExist());
    verify(problemService).getProblem(projectId, analysisId, );
  }

}

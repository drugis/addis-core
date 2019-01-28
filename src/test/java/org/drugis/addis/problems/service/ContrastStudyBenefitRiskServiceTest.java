package org.drugis.addis.problems.service;

import org.drugis.addis.analyses.model.BenefitRiskStudyOutcomeInclusion;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.problems.model.SingleStudyContext;
import org.drugis.addis.problems.service.impl.ContrastStudyBenefitRiskServiceImpl;
import org.drugis.addis.problems.service.model.*;
import org.drugis.addis.trialverse.model.trialdata.Measurement;
import org.drugis.addis.trialverse.model.trialdata.TrialDataArm;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ContrastStudyBenefitRiskServiceTest {
  @InjectMocks
  private ContrastStudyBenefitRiskService contrastStudyBenefitRiskService;

  @Before
  public void setUp() {
    contrastStudyBenefitRiskService = new ContrastStudyBenefitRiskServiceImpl();
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void buildContrastPerformanceTableTest() {
    URI defaultMoment = URI.create("defaultMoment");

    List<TrialDataArm> matchedArms = new ArrayList<>();
    TrialDataArm nonReferenceArm = mock(TrialDataArm.class);
    TrialDataArm referenceArm = mock(TrialDataArm.class);
    URI armUri1 = URI.create("armUri1");
    URI referenceArmUri = URI.create("referenceArmUri");
    Set<Measurement> measurements = new HashSet<>();
    Measurement measurement = mock(Measurement.class);
    measurements.add(measurement);

    matchedArms.add(nonReferenceArm);
    matchedArms.add(referenceArm);

    SingleStudyContext context = new SingleStudyContext();
    Outcome outcome = mock(Outcome.class);
    Integer outcomeId = 37;
    URI semanticOutcomeUri = URI.create("semanticOutcomeUri");
    String dataSourceUuid = "dataSourceUuid";

    Set<Integer> matchedProjectInterventionIds = new HashSet<>();
    Integer interventionId = 1;
    matchedProjectInterventionIds.add(interventionId);

    Set<Integer> matchedReferenceProjectInterventionIds = new HashSet<>();
    Integer referenceInterventionId = 2;
    matchedReferenceProjectInterventionIds.add(referenceInterventionId);

    when(nonReferenceArm.getUri()).thenReturn(armUri1);
    when(nonReferenceArm.getMeasurementsForMoment(defaultMoment)).thenReturn(measurements);
    when(nonReferenceArm.getMatchedProjectInterventionIds()).thenReturn(matchedProjectInterventionIds);
    when(referenceArm.getUri()).thenReturn(referenceArmUri);
    when(referenceArm.getMatchedProjectInterventionIds()).thenReturn(matchedReferenceProjectInterventionIds);
    when(measurement.getOddsRatio()).thenReturn(5.0);
    when(measurement.getVariableConceptUri()).thenReturn(semanticOutcomeUri);
    when(measurement.getReferenceArm()).thenReturn(referenceArmUri);
    when(measurement.getArmUri()).thenReturn(armUri1);
    when(outcome.getId()).thenReturn(outcomeId);
    when(outcome.getSemanticOutcomeUri()).thenReturn(semanticOutcomeUri);

    String baseline = "baselineString";
    URI studyGraphUri = URI.create("studyGraphUri");
    Integer analysisId = 42;
    BenefitRiskStudyOutcomeInclusion inclusion = new BenefitRiskStudyOutcomeInclusion(analysisId, outcomeId, studyGraphUri, baseline);

    context.setOutcome(outcome);
    context.setInclusion(inclusion);
    context.setDataSourceUuid(dataSourceUuid);

    List<AbstractMeasurementEntry> result = contrastStudyBenefitRiskService.buildContrastPerformanceTable(defaultMoment, context, matchedArms);

    List<AbstractMeasurementEntry> expectedResult = new ArrayList<>();
    String type = "relative-logit-normal";
    Map<String, Double> mu = new HashMap<>();
    mu.put(interventionId.toString(), 0.0);
    mu.put(referenceInterventionId.toString(), 0.0);
    List<String> rownames = new ArrayList<>();
    rownames.add(referenceInterventionId.toString());
    rownames.add(interventionId.toString());

    List<List<Double>> data = new ArrayList<>();
    List<Double> row1 = new ArrayList<>();
    List<Double> row2 = new ArrayList<>();
    row1.add(0.0);
    row1.add(0.0);
    row2.add(0.0);
    row2.add(5.0);
    data.add(row1);
    data.add(row2);
    CovarianceMatrix cov = new CovarianceMatrix(rownames, rownames, data);
    Relative relative = new Relative("dmnorm", mu, cov);
    RelativePerformanceParameters parameters = new RelativePerformanceParameters(baseline, relative);
    RelativePerformance performance = new RelativePerformance(type, parameters);
    AbstractMeasurementEntry expectedEntry = new RelativePerformanceEntry(semanticOutcomeUri.toString(), dataSourceUuid, performance);
    expectedResult.add(expectedEntry);
    assertEquals(expectedResult, result);
  }
}
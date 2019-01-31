package org.drugis.addis.problems.service;

import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.problems.model.SingleStudyContext;
import org.drugis.addis.problems.service.impl.AbsoluteStudyBenefitRiskServiceImpl;
import org.drugis.addis.trialverse.model.trialdata.Measurement;
import org.drugis.addis.trialverse.model.trialdata.TrialDataArm;
import org.drugis.addis.trialverse.model.trialdata.TrialDataStudy;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AbsoluteStudyBenefitRiskServiceImplTest {
  @InjectMocks
  private AbsoluteStudyBenefitRiskService absoluteStudyBenefitRiskService;

  @Before
  public void setUp() {
    absoluteStudyBenefitRiskService = new AbsoluteStudyBenefitRiskServiceImpl();
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void buildAbsolutePerformanceEntries() {
    URI defaultMeasurementMomentUri = URI.create("defaultMoment");
    URI dichotomousUri = URI.create("http://trials.drugis.org/ontology#dichotomous");
    URI continuousUri = URI.create("http://trials.drugis.org/ontology#continuous");
    URI measurementVariableConceptUri = URI.create("measurementVariableConceptUri");


    TrialDataArm arm1 = getTrialDataArm(defaultMeasurementMomentUri, dichotomousUri, measurementVariableConceptUri);
    TrialDataArm arm2 = getTrialDataArm(defaultMeasurementMomentUri, continuousUri, measurementVariableConceptUri);
    List<TrialDataArm> matchedArms = Arrays.asList(arm1, arm2);

    TrialDataStudy study = getTrialDataStudy(defaultMeasurementMomentUri);

    SingleStudyContext context = getSingleStudyContext();

    absoluteStudyBenefitRiskService.buildAbsolutePerformanceEntries(context, study, matchedArms);
  }

  private TrialDataArm getTrialDataArm(URI defaultMeasurementMomentUri, URI dichotomousUri, URI measurementVariableConceptUri) {
    Set<Integer> matchedProjectInterventionIds = new HashSet<>();
    Integer interventionId = 1;
    matchedProjectInterventionIds.add(interventionId);
    Measurement measurement1 = getMeasurement(dichotomousUri, measurementVariableConceptUri);
    TrialDataArm arm1 = new TrialDataArm();
    arm1.addMeasurement(defaultMeasurementMomentUri, measurement1);
    arm1.setMatchedProjectInterventionIds(matchedProjectInterventionIds);
    return arm1;
  }

  private TrialDataStudy getTrialDataStudy(URI defaultMeasurementMomentUri) {
    TrialDataStudy study = mock(TrialDataStudy.class);
    when(study.getDefaultMeasurementMoment()).thenReturn(defaultMeasurementMomentUri);
    return study;
  }

  private SingleStudyContext getSingleStudyContext() {
    SingleStudyContext context = mock(SingleStudyContext.class);
    Outcome outcome1 = mock(Outcome.class);
    when(context.getOutcome()).thenReturn(outcome1);
    String dataSourceId1 = "dataSource1";
    when(context.getDataSourceUuid()).thenReturn(dataSourceId1);
    return context;
  }

  private Measurement getMeasurement(URI dichotomousUri, URI measurementVariableConceptUri) {
    Measurement measurement1 = mock(Measurement.class);
    when(measurement1.getVariableConceptUri()).thenReturn(measurementVariableConceptUri);
    when(measurement1.getReferenceArm()).thenReturn(null);
    when(measurement1.getMeasurementTypeURI()).thenReturn(dichotomousUri);
    return measurement1;
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUnknownMeasurementTypeThrows() {
    URI UNKNOWN_TYPE = URI.create("unknown");
    URI defaultMeasurementMomentUri = URI.create("defaultMoment");
    URI measurementVariableConceptUri = URI.create("measurementVariableConceptUri");

    SingleStudyContext context = getSingleStudyContext();
    TrialDataStudy study = getTrialDataStudy(defaultMeasurementMomentUri);
    TrialDataArm arm = getTrialDataArm(defaultMeasurementMomentUri, UNKNOWN_TYPE, measurementVariableConceptUri);
    List<TrialDataArm> matchedArms = new ArrayList<>();
    matchedArms.add(arm);

    absoluteStudyBenefitRiskService.buildAbsolutePerformanceEntries(context, study, matchedArms);
  }
}
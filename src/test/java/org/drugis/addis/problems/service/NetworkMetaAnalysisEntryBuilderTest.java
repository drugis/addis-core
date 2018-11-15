package org.drugis.addis.problems.service;

import org.drugis.addis.problems.model.*;
import org.drugis.addis.problems.service.impl.NetworkMetaAnalysisEntryBuilder;
import org.drugis.addis.trialverse.model.trialdata.AbsoluteMeasurement;
import org.junit.Test;

import java.net.URI;

import static org.drugis.addis.problems.service.ProblemService.CONTINUOUS_TYPE_URI;
import static org.drugis.addis.problems.service.ProblemService.DICHOTOMOUS_TYPE_URI;
import static org.drugis.addis.problems.service.ProblemService.SURVIVAL_TYPE_URI;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NetworkMetaAnalysisEntryBuilderTest {

  private NetworkMetaAnalysisEntryBuilder builder = new NetworkMetaAnalysisEntryBuilder();
  private String studyName = "studyName";
  private Integer treatmentId = -3;
  private final Integer sampleSize = 42;

  @Test
  public void testBuildContinuousStdErrEntry() {
    AbsoluteMeasurement measurement = mock(AbsoluteMeasurement.class);
    Double mean = 1.0;
    Double stdErr = 0.5;

    when(measurement.getMeasurementTypeURI()).thenReturn(CONTINUOUS_TYPE_URI);
    when(measurement.getMean()).thenReturn(mean);
    when(measurement.getStdErr()).thenReturn(stdErr);
    when(measurement.getStdDev()).thenReturn(null);

    AbstractNetworkMetaAnalysisProblemEntry result = builder.build(studyName, treatmentId, measurement);
    ContinuousStdErrEntry expectedResult = new ContinuousStdErrEntry(studyName, treatmentId, mean, stdErr);
    assertEquals(expectedResult, result);
  }

  @Test
  public void testBuildContinuousStdDevEntry(){
    AbsoluteMeasurement measurement = mock(AbsoluteMeasurement.class);
    Double mean = 1.0;
    Double stdDev= 0.5;

    when(measurement.getMeasurementTypeURI()).thenReturn(CONTINUOUS_TYPE_URI);
    when(measurement.getSampleSize()).thenReturn(sampleSize);
    when(measurement.getMean()).thenReturn(mean);
    when(measurement.getStdDev()).thenReturn(stdDev);

    AbstractNetworkMetaAnalysisProblemEntry result = builder.build(studyName, treatmentId, measurement);
    ContinuousNetworkMetaAnalysisProblemEntry expectedResult = new ContinuousNetworkMetaAnalysisProblemEntry(studyName, treatmentId, sampleSize, mean, stdDev);
    assertEquals(expectedResult, result);
  }

  @Test
  public void testBuildDichotomousEntry(){
    AbsoluteMeasurement measurement = mock(AbsoluteMeasurement.class);
    Integer rate = 10;

    when(measurement.getSampleSize()).thenReturn(sampleSize);
    when(measurement.getRate()).thenReturn(rate);
    when(measurement.getMeasurementTypeURI()).thenReturn(DICHOTOMOUS_TYPE_URI);

    AbstractNetworkMetaAnalysisProblemEntry result = builder.build(studyName, treatmentId, measurement);
    RateNetworkMetaAnalysisProblemEntry expectedResult = new RateNetworkMetaAnalysisProblemEntry(studyName, treatmentId, sampleSize, rate);
    assertEquals(expectedResult, result);
  }

  @Test
  public void testBuildSurvivalEntry(){
    AbsoluteMeasurement measurement = mock(AbsoluteMeasurement.class);
    Integer rate = 10;
    Double exposure = 0.7;
    String timeScale = "days";

    when(measurement.getRate()).thenReturn(rate);
    when(measurement.getExposure()).thenReturn(exposure);
    when(measurement.getSurvivalTimeScale()).thenReturn(timeScale);
    when(measurement.getMeasurementTypeURI()).thenReturn(SURVIVAL_TYPE_URI);

    AbstractNetworkMetaAnalysisProblemEntry result = builder.build(studyName, treatmentId, measurement);
    SurvivalEntry expectedResult = new SurvivalEntry(studyName, treatmentId, timeScale, rate, exposure);
    assertEquals(expectedResult, result);
  }

  @Test(expected = RuntimeException.class)
  public void testUnknownMeasurementTypeThrows() {
    AbsoluteMeasurement measurement = mock(AbsoluteMeasurement.class);
    when(measurement.getMeasurementTypeURI()).thenReturn(URI.create("eatAtJoes"));
    builder.build(studyName, treatmentId, measurement);
  }

}
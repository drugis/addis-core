package org.drugis.addis.problems.service;

import org.drugis.addis.problems.model.problemEntry.*;
import org.drugis.addis.problems.service.impl.NetworkMetaAnalysisEntryBuilder;
import org.drugis.addis.trialverse.model.trialdata.Measurement;
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
  private final Double stdErr = 3.2;
  private final URI referenceArm = URI.create(("http://referenceArm"));


  @Test
  public void testBuildContinuousStdErrEntry() {
    Measurement measurement = mock(Measurement.class);
    Double mean = 1.0;
    Double stdErr = 0.5;

    when(measurement.getMeasurementTypeURI()).thenReturn(CONTINUOUS_TYPE_URI);
    when(measurement.getMean()).thenReturn(mean);
    when(measurement.getStdErr()).thenReturn(stdErr);
    when(measurement.getStdDev()).thenReturn(null);

    AbstractProblemEntry result = builder.buildAbsoluteEntry(studyName, treatmentId, measurement);
    AbsoluteContinuousStdErrProblemEntry expectedResult = new AbsoluteContinuousStdErrProblemEntry(studyName, treatmentId, mean, stdErr);
    assertEquals(expectedResult, result);
  }

  @Test
  public void testBuildContinuousStdDevEntry() {
    Measurement measurement = mock(Measurement.class);
    Double mean = 1.0;
    Double stdDev = 0.5;

    when(measurement.getMeasurementTypeURI()).thenReturn(CONTINUOUS_TYPE_URI);
    when(measurement.getSampleSize()).thenReturn(sampleSize);
    when(measurement.getMean()).thenReturn(mean);
    when(measurement.getStdDev()).thenReturn(stdDev);

    AbstractProblemEntry result = builder.buildAbsoluteEntry(studyName, treatmentId, measurement);
    AbsoluteContinuousProblemEntry expectedResult = new AbsoluteContinuousProblemEntry(studyName, treatmentId, sampleSize, mean, stdDev);
    assertEquals(expectedResult, result);
  }

  @Test
  public void testBuildDichotomousEntry() {
    Measurement measurement = mock(Measurement.class);
    Integer rate = 10;

    when(measurement.getSampleSize()).thenReturn(sampleSize);
    when(measurement.getRate()).thenReturn(rate);
    when(measurement.getMeasurementTypeURI()).thenReturn(DICHOTOMOUS_TYPE_URI);

    AbstractProblemEntry result = builder.buildAbsoluteEntry(studyName, treatmentId, measurement);
    AbsoluteDichotomousProblemEntry expectedResult = new AbsoluteDichotomousProblemEntry(studyName, treatmentId, sampleSize, rate);
    assertEquals(expectedResult, result);
  }

  @Test
  public void testBuildSurvivalEntry() {
    Measurement measurement = mock(Measurement.class);
    Integer rate = 10;
    Double exposure = 0.7;
    String timeScale = "days";

    when(measurement.getRate()).thenReturn(rate);
    when(measurement.getExposure()).thenReturn(exposure);
    when(measurement.getSurvivalTimeScale()).thenReturn(timeScale);
    when(measurement.getMeasurementTypeURI()).thenReturn(SURVIVAL_TYPE_URI);

    AbstractProblemEntry result = builder.buildAbsoluteEntry(studyName, treatmentId, measurement);
    AbsoluteSurvivalProblemEntry expectedResult = new AbsoluteSurvivalProblemEntry(studyName,
            treatmentId, timeScale, rate, exposure);
    assertEquals(expectedResult, result);
  }

  @Test(expected = RuntimeException.class)
  public void testUnknownMeasurementTypeThrows() {
    Measurement measurement = mock(Measurement.class);
    when(measurement.getMeasurementTypeURI()).thenReturn(URI.create("eatAtJoes"));
    builder.buildAbsoluteEntry(studyName, treatmentId, measurement);
  }

  @Test
  public void testBuildSMDEntryFromStdErr() {
    Measurement measurement = buildContinuousContrastMeasurementBase(stdErr);
    Double smd = 4.5;
    when(measurement.getStandardizedMeanDifference()).thenReturn(smd);
    AbstractProblemEntry entry = builder.buildContrastEntry(studyName, treatmentId, measurement);

    ContrastSMDProblemEntry expected = new ContrastSMDProblemEntry(studyName, treatmentId, smd, stdErr);
    assertEquals(expected, entry);
  }

  @Test
  public void testBuildSMDEntryFromCI() {
    Measurement measurement = buildContinuousContrastMeasurementBase(null);
    Double smd = 4.5;
    Double stdErr = 2.0;
    when(measurement.getStdErr()).thenReturn(stdErr);
    when(measurement.getStandardizedMeanDifference()).thenReturn(smd);
    AbstractProblemEntry entry = builder.buildContrastEntry(studyName, treatmentId, measurement);

    ContrastSMDProblemEntry expected = new ContrastSMDProblemEntry(studyName, treatmentId, smd, stdErr);
    assertEquals(expected, entry);
  }

  @Test
  public void testBuildMDEntryFromStdErr() {
    Double md = 4.5;
    Measurement measurement = buildContinuousContrastMeasurementBase(stdErr);
    when(measurement.getMeanDifference()).thenReturn(md);
    AbstractProblemEntry entry = builder.buildContrastEntry(studyName, treatmentId, measurement);

    ContrastMDProblemEntry expected = new ContrastMDProblemEntry(studyName, treatmentId, md, stdErr);
    assertEquals(expected, entry);
  }

  @Test
  public void testBuildOddsRatioEntry() {
    Double oddsRatio = 2.3;
    Measurement measurement = buildDichotomousContrastMeasurementBase(stdErr);
    when(measurement.getOddsRatio()).thenReturn(oddsRatio);

    AbstractProblemEntry entry = builder.buildContrastEntry(studyName, treatmentId, measurement);

    ContrastDichotomousOddsProblemEntry expected = new ContrastDichotomousOddsProblemEntry(studyName, treatmentId, oddsRatio, stdErr);
    assertEquals(expected, entry);
  }

  @Test
  public void testBuildRiskRatioEntry() {
    Double riskRatio = 2.3;
    Measurement measurement = buildDichotomousContrastMeasurementBase(stdErr);
    when(measurement.getOddsRatio()).thenReturn(null);
    when(measurement.getRiskRatio()).thenReturn(riskRatio);

    AbstractProblemEntry entry = builder.buildContrastEntry(studyName, treatmentId, measurement);

    ContrastDichotomousRiskProblemEntry expected = new ContrastDichotomousRiskProblemEntry(studyName, treatmentId, riskRatio, stdErr);
    assertEquals(expected, entry);
  }

  @Test
  public void testBuildHazardRatioEntry() {
    Double hazardRatio = 2.3;
    Measurement measurement = buildSurvivalContrastMeasurementBase(stdErr);
    when(measurement.getHazardRatio()).thenReturn(hazardRatio);

    AbstractProblemEntry entry = builder.buildContrastEntry(studyName, treatmentId, measurement);

    ContrastSurvivalHazardProblemEntry expected = new ContrastSurvivalHazardProblemEntry(studyName, treatmentId, hazardRatio, stdErr);
    assertEquals(expected, entry);
  }

  private Measurement buildDichotomousContrastMeasurementBase(Double stdErr) {
    return buildContrastMeasurementBase(stdErr, DICHOTOMOUS_TYPE_URI);
  }

  private Measurement buildContinuousContrastMeasurementBase(Double stdErr) {
    return buildContrastMeasurementBase(stdErr, CONTINUOUS_TYPE_URI);
  }

  private Measurement buildSurvivalContrastMeasurementBase(Double stdErr) {
    return buildContrastMeasurementBase(stdErr, SURVIVAL_TYPE_URI);
  }

  private Measurement buildContrastMeasurementBase(Double stdErr, URI measurementTypeUri) {
    Measurement measurement = mock(Measurement.class);
    when(measurement.getMeasurementTypeURI()).thenReturn(measurementTypeUri);
    when(measurement.getStdErr()).thenReturn(stdErr);
    when(measurement.getReferenceArm()).thenReturn(referenceArm);
    when(measurement.getReferenceStdErr()).thenReturn(null);
    when(measurement.getStandardizedMeanDifference()).thenReturn(null);
    return measurement;
  }

}
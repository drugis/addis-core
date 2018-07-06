package org.drugis.addis.problems;

import org.apache.jena.ext.com.google.common.collect.ImmutableSet;
import org.drugis.addis.problems.model.Arm;
import org.drugis.addis.problems.model.MeasurementType;
import org.drugis.addis.problems.model.MeasurementWithCoordinates;
import org.drugis.addis.problems.model.Variable;
import org.drugis.addis.problems.service.impl.SingleStudyPerformanceTableBuilder;
import org.drugis.addis.problems.service.model.*;
import org.drugis.addis.trialverse.model.trialdata.Measurement;
import org.drugis.addis.trialverse.model.trialdata.MeasurementBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.util.List;
import java.util.Set;

import static org.drugis.addis.problems.service.ProblemService.CONTINUOUS_TYPE_URI;
import static org.drugis.addis.problems.service.ProblemService.DICHOTOMOUS_TYPE_URI;
import static org.junit.Assert.assertEquals;

/**
 * Created by daan on 3/27/14.
 */
public class SingleStudyPerformanceTableBuilderTest {

  @InjectMocks
  private SingleStudyPerformanceTableBuilder builder;

  private String armName1 = "arm name 1";
  private String armName2 = "arm name 2";
  private Arm arm1 = new Arm(URI.create("armUri1"), "drugUuid1", armName1);
  private Arm arm2 = new Arm(URI.create("armUri2"), "drugUuid2", armName2);

  private URI criterionUri1 = URI.create("critUri1");
  private URI criterionUri2 = URI.create("critUri2");

  private String variableName1 = "variable name 1";
  private String variableName2 = "variable name 2";

  private String studyUuid = "aa-bb";
  private URI studyUri = URI.create(studyUuid);
  private String dataSourceUuid = "dataSource1";

  private Variable continuousVariable = new Variable(URI.create("variableUri1"), studyUuid, variableName1, "desc", null, false, MeasurementType.RATE, URI.create("varConcept1"));
  private Variable dichotomousVariable = new Variable(URI.create("variableUri2"), studyUuid, variableName2, "desc", null, false, MeasurementType.CONTINUOUS, URI.create("varConcept2"));

  private Measurement dichotomousMeasurement = new MeasurementBuilder(studyUri, dichotomousVariable.getUri(), dichotomousVariable.getVariableConceptUri(), arm1.getUri(), DICHOTOMOUS_TYPE_URI)
          .setSampleSize(111).setRate(42).createMeasurement();
  private Measurement continuousMeasurementStdDev = new MeasurementBuilder(studyUri, continuousVariable.getUri(), continuousVariable.getVariableConceptUri(), arm1.getUri(), CONTINUOUS_TYPE_URI)
          .setSampleSize(222).setStdDev(0.2).setMean(7.56).createMeasurement();
  private Measurement continuousMeasurementStdErr = new MeasurementBuilder(studyUri, continuousVariable.getUri(), continuousVariable.getVariableConceptUri(), arm1.getUri(), CONTINUOUS_TYPE_URI)
          .setSampleSize(333).setStdErr(0.3).setMean(7.56).createMeasurement();

  @Before
  public void setUp() {
    builder = new SingleStudyPerformanceTableBuilder();
    MockitoAnnotations.initMocks(this);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testBuild() {

    Integer rate = dichotomousMeasurement.getRate();

    Double mu = continuousMeasurementStdDev.getMean();
    Double stdDev = continuousMeasurementStdDev.getStdDev();
    Double stdErr = continuousMeasurementStdErr.getStdErr();

    URI studyUri = URI.create("itsastudio");
    Integer alternativeId1 = 1;
    Integer alternativeId2 = 2;

    MeasurementBuilder continuousStdDevBuilder = new MeasurementBuilder(studyUri, criterionUri1, continuousMeasurementStdDev.getVariableConceptUri(), arm1.getUri(), CONTINUOUS_TYPE_URI)
            .setSampleSize(continuousMeasurementStdDev.getSampleSize())
            .setStdDev(stdDev)
            .setMean(mu);
    MeasurementBuilder dichotomousBuilder = new MeasurementBuilder(studyUri, criterionUri2, dichotomousMeasurement.getVariableConceptUri(), arm2.getUri(), DICHOTOMOUS_TYPE_URI)
            .setSampleSize(dichotomousMeasurement.getSampleSize())
            .setRate(rate);

    Measurement arm1ContinuousStdDev = continuousStdDevBuilder.createMeasurement();
    continuousStdDevBuilder.setArmUri(arm2.getUri());
    Measurement arm2ContinuousStdDev = continuousStdDevBuilder.createMeasurement();

    MeasurementWithCoordinates continuousStdDevRow1 = new MeasurementWithCoordinates(arm1ContinuousStdDev, alternativeId1, dataSourceUuid);
    MeasurementWithCoordinates continuousStdDevRow2 = new MeasurementWithCoordinates(arm2ContinuousStdDev, alternativeId2, dataSourceUuid);

    Measurement arm1Dichotomous = dichotomousBuilder.createMeasurement();
    dichotomousBuilder.setArmUri(arm2.getUri());
    Measurement arm2Dichotomous = dichotomousBuilder.createMeasurement();
    MeasurementWithCoordinates dichotomousRow1 = new MeasurementWithCoordinates(arm1Dichotomous, alternativeId1, dataSourceUuid);
    MeasurementWithCoordinates dichotomousRow2 = new MeasurementWithCoordinates(arm2Dichotomous, alternativeId2, dataSourceUuid);

    MeasurementBuilder continuousStdErrBuilder = new MeasurementBuilder(studyUri, criterionUri1, continuousMeasurementStdErr.getVariableConceptUri(), arm1.getUri(), CONTINUOUS_TYPE_URI)
            .setSampleSize(continuousMeasurementStdErr.getSampleSize())
            .setStdErr(stdErr);
    Measurement arm1ContinuousStdErr = continuousStdErrBuilder.createMeasurement();
    continuousStdErrBuilder.setArmUri(arm2.getUri());
    Measurement arm2ContinuousStdErr = continuousStdErrBuilder.createMeasurement();
    MeasurementWithCoordinates continuousStdErrRow1 = new MeasurementWithCoordinates(arm1ContinuousStdErr, alternativeId1, dataSourceUuid);
    MeasurementWithCoordinates continuousStdErrRow2 = new MeasurementWithCoordinates(arm2ContinuousStdErr, alternativeId2, dataSourceUuid);

    Set<MeasurementWithCoordinates> measurementsWithCoordinates = ImmutableSet.of(continuousStdDevRow1, continuousStdDevRow2, dichotomousRow1, dichotomousRow2, continuousStdErrRow1, continuousStdErrRow2);

    // EXECUTE
    List<AbstractMeasurementEntry> performanceTable = builder.build(measurementsWithCoordinates);

    // ASSERTS
    assertEquals(6, performanceTable.size());

    ContinuousMeasurementEntry continuousMeasurementEntry = (ContinuousMeasurementEntry) performanceTable.get(0);
    assertEquals(alternativeId1.toString(), continuousMeasurementEntry.getAlternative());
    assertEquals(continuousVariable.getVariableConceptUri().toString(), continuousMeasurementEntry.getCriterion());
    assertEquals(ContinuousPerformance.DNORM, continuousMeasurementEntry.getPerformance().getType());
    assertEquals(dataSourceUuid, continuousMeasurementEntry.getDataSource());

    Double expectedMu = continuousMeasurementStdDev.getMean();
    Integer expectedSampleSize = continuousMeasurementStdDev.getSampleSize();
    Double expectedSigma = continuousMeasurementStdDev.getStdDev() / Math.sqrt(expectedSampleSize);

    ContinuousPerformanceParameters parameters = continuousMeasurementEntry.getPerformance().getParameters();
    assertEquals(expectedMu, parameters.getMu());
    assertEquals(expectedSigma, parameters.getSigma());
    assertEquals(ContinuousPerformance.DNORM, continuousMeasurementEntry.getPerformance().getType());

    RateMeasurementEntry rateMeasurementEntry = (RateMeasurementEntry) performanceTable.get(2);
    assertEquals(alternativeId1.toString(), rateMeasurementEntry.getAlternative());
    assertEquals(dichotomousVariable.getVariableConceptUri().toString(), rateMeasurementEntry.getCriterion());
    assertEquals(RatePerformance.DBETA, rateMeasurementEntry.getPerformance().getType());

    Integer expectedAlpha = dichotomousMeasurement.getRate() + 1;
    Integer expectedBeta = dichotomousMeasurement.getSampleSize() - dichotomousMeasurement.getRate() + 1;
    assertEquals(expectedAlpha, rateMeasurementEntry.getPerformance().getParameters().getAlpha());
    assertEquals(expectedBeta, rateMeasurementEntry.getPerformance().getParameters().getBeta());
    assertEquals(RatePerformance.DBETA, rateMeasurementEntry.getPerformance().getType());

    ContinuousMeasurementEntry stdErrBasedEntry = (ContinuousMeasurementEntry) performanceTable.get(4);
    assertEquals(ContinuousPerformance.DNORM, stdErrBasedEntry.getPerformance().getType());
    assertEquals(continuousMeasurementStdErr.getStdErr(), stdErrBasedEntry.getPerformance().getParameters().getSigma());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUnknownMeasurementTypeThrows() {
    Integer alternativeId1 = 1;
    URI UNKNOWN_TYPE = URI.create("unknown");
    MeasurementBuilder unknownBuilder = new MeasurementBuilder(studyUri,continuousVariable.getUri(), continuousVariable.getVariableConceptUri() ,arm1.getUri(), UNKNOWN_TYPE);
    Measurement arm1Unknown = unknownBuilder.createMeasurement();
    MeasurementWithCoordinates unknownRow1 = new MeasurementWithCoordinates(arm1Unknown, alternativeId1, dataSourceUuid);
    Set<MeasurementWithCoordinates> measurementsWithUnknownType = ImmutableSet.of(unknownRow1);
    builder.build(measurementsWithUnknownType);
  }

}

package org.drugis.addis.problems;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.jena.ext.com.google.common.collect.ImmutableSet;
import org.drugis.addis.problems.model.Arm;
import org.drugis.addis.problems.model.MeasurementType;
import org.drugis.addis.problems.model.Variable;
import org.drugis.addis.problems.service.impl.SingleStudyPerformanceTableBuilder;
import org.drugis.addis.problems.service.model.*;
import org.drugis.addis.trialverse.model.trialdata.Measurement;
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
  private Arm arm1 = new Arm(URI.create("1L"), "10L", armName1);
  private Arm arm2 = new Arm(URI.create("2L"), "11L", armName2);

  private URI criterionUri1 = URI.create("critUri1");
  private URI criterionUri2 = URI.create("critUri2");

  private String variableName1 = "variable name 1";
  private String variableName2 = "variable name 2";

  private Variable variable1 = new Variable(URI.create("101L"), "1L", variableName1, "desc", null, false, MeasurementType.RATE, URI.create("varConcept1"));
  private Variable variable2 = new Variable(URI.create("102L"), "1L", variableName2, "desc", null, false, MeasurementType.CONTINUOUS, URI.create("varConcept2"));


  private Measurement dichotomousMeasurement = new Measurement(URI.create("1L"), variable1.getUri(), variable1.getVariableConceptUri(), null,
          arm1.getUri(), DICHOTOMOUS_TYPE_URI, 111, 42, null, null, null, null);
  private Measurement continuousMeasurementStdDev = new Measurement(URI.create("1L"), variable2.getUri(), variable2.getVariableConceptUri(), null,
          arm1.getUri(), CONTINUOUS_TYPE_URI, 222, null, 0.2, null, 7.56, null);
  private Measurement continuousMeasurementStdErr = new Measurement(URI.create("1L"), variable2.getUri(), variable2.getVariableConceptUri(), null,
          arm1.getUri(), CONTINUOUS_TYPE_URI, 333, null, null, 0.3, 7.56, null);

  @Before
  public void setUp() throws Exception {
    builder = new SingleStudyPerformanceTableBuilder();
    MockitoAnnotations.initMocks(this);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testBuild() throws Exception {

    Integer rate = dichotomousMeasurement.getRate();
    Integer sampleSize1 = continuousMeasurementStdDev.getSampleSize();
    Integer sampleSize3 = continuousMeasurementStdErr.getSampleSize();

    Integer sampleSize2 = dichotomousMeasurement.getSampleSize();
    Double mu = continuousMeasurementStdDev.getMean();
    Double stdDev = continuousMeasurementStdDev.getStdDev();
    Double stdErr = continuousMeasurementStdErr.getStdErr();

    URI studyUri = URI.create("itsastudio");
    Integer alternativeUri1 = 1;
    Triple<Measurement, Integer, String> row1 = Triple.of(new Measurement(studyUri, criterionUri1, dichotomousMeasurement.getVariableConceptUri(), null,
            arm1.getUri(), CONTINUOUS_TYPE_URI, sampleSize1, null, stdDev, null, mu, null), alternativeUri1, "");
    Integer alternativeUri2 = 2;
    Triple<Measurement, Integer, String> row2 = Triple.of(new Measurement(studyUri, criterionUri2, continuousMeasurementStdDev.getVariableConceptUri(), null,
            arm2.getUri(), DICHOTOMOUS_TYPE_URI, sampleSize2, rate, null, null, null, null), alternativeUri2, "");
    Triple<Measurement, Integer, String> row3 = Triple.of(new Measurement(studyUri, criterionUri1, dichotomousMeasurement.getVariableConceptUri(), null,
            arm2.getUri(), CONTINUOUS_TYPE_URI, sampleSize1, null, stdDev, null, mu, null), alternativeUri2, "");
    Triple<Measurement, Integer, String> row4 = Triple.of(new Measurement(studyUri, criterionUri2, continuousMeasurementStdDev.getVariableConceptUri(), null,
            arm1.getUri(), DICHOTOMOUS_TYPE_URI, sampleSize2, rate, null, null, null, null), alternativeUri1, "");
    Triple<Measurement, Integer, String> row5 = Triple.of(new Measurement(studyUri, criterionUri1, continuousMeasurementStdErr.getVariableConceptUri(), null,
            arm1.getUri(), CONTINUOUS_TYPE_URI, sampleSize3, null, null, stdErr, null, null), alternativeUri1, "");
    Triple<Measurement, Integer, String> row6 = Triple.of(new Measurement(studyUri, criterionUri2, continuousMeasurementStdErr.getVariableConceptUri(), null,
            arm1.getUri(), CONTINUOUS_TYPE_URI, sampleSize3, null, null, stdErr, null, null), alternativeUri2, "");

    // EXECUTE
    Set<Triple<Measurement, Integer, String>> measurementPairs = ImmutableSet.of(row1, row2, row3, row4, row5, row6);
    List<AbstractMeasurementEntry> performanceTable = builder.build(measurementPairs);

    assertEquals(6, performanceTable.size());

    ContinuousMeasurementEntry continuousMeasurementEntry = (ContinuousMeasurementEntry) performanceTable.get(0);
    assertEquals(alternativeUri1.toString(), continuousMeasurementEntry.getAlternative());
    assertEquals(variable1.getVariableConceptUri().toString(), continuousMeasurementEntry.getCriterion());
    assertEquals(ContinuousPerformance.DNORM, continuousMeasurementEntry.getPerformance().getType());

    RateMeasurementEntry rateMeasurementEntry = (RateMeasurementEntry) performanceTable.get(1);
    assertEquals(alternativeUri2.toString(), rateMeasurementEntry.getAlternative());
    assertEquals(variable2.getVariableConceptUri().toString(), rateMeasurementEntry.getCriterion());
    assertEquals(RatePerformance.DBETA, rateMeasurementEntry.getPerformance().getType());

    Integer expectedAlpha = dichotomousMeasurement.getRate() + 1;
    Integer expectedBeta = dichotomousMeasurement.getSampleSize() - dichotomousMeasurement.getRate() + 1;
    assertEquals(expectedAlpha, rateMeasurementEntry.getPerformance().getParameters().getAlpha());
    assertEquals(expectedBeta, rateMeasurementEntry.getPerformance().getParameters().getBeta());
    assertEquals(RatePerformance.DBETA, rateMeasurementEntry.getPerformance().getType());

    Double expectedMu = continuousMeasurementStdDev.getMean();
    Integer expectedSampleSize = continuousMeasurementStdDev.getSampleSize();
    Double expectedSigma = continuousMeasurementStdDev.getStdDev() / Math.sqrt(expectedSampleSize);

    ContinuousPerformanceParameters parameters = continuousMeasurementEntry.getPerformance().getParameters();
    assertEquals(expectedMu, parameters.getMu());
    assertEquals(expectedSigma, parameters.getSigma());
    assertEquals(ContinuousPerformance.DNORM, continuousMeasurementEntry.getPerformance().getType());

    ContinuousMeasurementEntry stdErrBasedEntry = (ContinuousMeasurementEntry) performanceTable.get(4);
    assertEquals(ContinuousPerformance.DNORM, stdErrBasedEntry.getPerformance().getType());
    assertEquals(continuousMeasurementStdErr.getStdErr(), stdErrBasedEntry.getPerformance().getParameters().getSigma());

  }

}

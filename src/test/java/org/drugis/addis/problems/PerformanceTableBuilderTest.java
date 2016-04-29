package org.drugis.addis.problems;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.ext.com.google.common.collect.ImmutableSet;
import org.drugis.addis.problems.model.Arm;
import org.drugis.addis.problems.model.MeasurementType;
import org.drugis.addis.problems.model.Variable;
import org.drugis.addis.problems.service.impl.PerformanceTableBuilder;
import org.drugis.addis.problems.service.model.*;
import org.drugis.addis.trialverse.model.trialdata.Measurement;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Created by daan on 3/27/14.
 */
public class PerformanceTableBuilderTest {

  @InjectMocks
  private PerformanceTableBuilder builder;

  private String armName1 = "arm name 1";
  private String armName2 = "arm name 2";
  private Arm arm1 = new Arm(URI.create("1L"), "10L", armName1);
  private Arm arm2 = new Arm(URI.create("2L"), "11L", armName2);

  private Integer alternativeUri1 =1;
  private Integer alternativeUri2 =2;
  private URI criterionUri1 = URI.create("critUri1");
  private URI criterionUri2 = URI.create("critUri2");

  private String variableName1 = "variable name 1";
  private String variableName2 = "variable name 2";

  private Variable variable1 = new Variable(URI.create("101L"), "1L", variableName1, "desc", null, false, MeasurementType.RATE, URI.create("varConcept1"));
  private Variable variable2 = new Variable(URI.create("102L"), "1L", variableName2, "desc", null, false, MeasurementType.CONTINUOUS, URI.create("varConcept2"));

  private Measurement measurement1 = new Measurement(URI.create("1L"), variable1.getUri(), variable1.getVariableConceptUri(), arm1.getUri(), 111, 42, null, null);
  private Measurement measurement2 = new Measurement(URI.create("1L"), variable2.getUri(), variable2.getVariableConceptUri(), arm1.getUri(), 222, null, 0.2, 7.56);

  @Before
  public void setUp() throws Exception {
    builder = new PerformanceTableBuilder();
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testBuild() throws Exception {

    Integer rate = measurement1.getRate();
    Integer sampleSize1 = measurement2.getSampleSize();
    Integer alpha = rate + 1;
    Integer beta = sampleSize1 + 1;

    Integer sampleSize2 = measurement1.getSampleSize();
    Double mu = measurement2.getMean();
    Double stdDev = measurement2.getStdDev();

    URI studyUri = URI.create("itsastudio");
    Pair<Measurement, Integer> row1 = Pair.of(new Measurement(studyUri, criterionUri1, measurement1.getVariableConceptUri(), arm1.getUri(), sampleSize1, null, stdDev, mu), alternativeUri1);
    Pair<Measurement, Integer> row2 = Pair.of(new Measurement(studyUri, criterionUri2, measurement2.getVariableConceptUri(), arm2.getUri(), sampleSize2, rate, null, null), alternativeUri2);
    Pair<Measurement, Integer> row3 = Pair.of(new Measurement(studyUri, criterionUri1, measurement1.getVariableConceptUri(), arm2.getUri(), sampleSize1, null, stdDev, mu), alternativeUri2);
    Pair<Measurement, Integer> row4 = Pair.of(new Measurement(studyUri, criterionUri2, measurement2.getVariableConceptUri(), arm1.getUri(), sampleSize2, rate, null, null), alternativeUri1);

    // EXECUTE
    Set<Pair<Measurement, Integer>> measurementPairs = ImmutableSet.of(row1, row2, row3, row4);
    List<AbstractMeasurementEntry> performanceTable = builder.build(measurementPairs);

    assertEquals(4, performanceTable.size());

    ContinuousMeasurementEntry continuousMeasurementEntry = (ContinuousMeasurementEntry) performanceTable.get(0);
    assertEquals(alternativeUri1.toString(), continuousMeasurementEntry.getAlternative());
    assertEquals(criterionUri1, continuousMeasurementEntry.getCriterionUri());
    assertEquals(ContinuousPerformance.DNORM, continuousMeasurementEntry.getPerformance().getType());

    RateMeasurementEntry rateMeasurementEntry = (RateMeasurementEntry) performanceTable.get(1);
    assertEquals(alternativeUri2.toString(), rateMeasurementEntry.getAlternative());
    assertEquals(criterionUri2, rateMeasurementEntry.getCriterionUri());
    assertEquals(RatePerformance.DBETA, rateMeasurementEntry.getPerformance().getType());

    Integer expectedAlpha = measurement1.getRate() + 1;
    Integer expectedBeta = measurement1.getSampleSize() - measurement1.getRate() + 1;
    assertEquals(expectedAlpha, rateMeasurementEntry.getPerformance().getParameters().getAlpha());
    assertEquals(expectedBeta, rateMeasurementEntry.getPerformance().getParameters().getBeta());
    assertEquals(RatePerformance.DBETA, rateMeasurementEntry.getPerformance().getType());

    Double expectedMu = measurement2.getMean();
    Integer expectedSampleSize = measurement2.getSampleSize();
    Double expectedSigma = measurement2.getStdDev() / Math.sqrt(expectedSampleSize);

    ContinuousPerformanceParameters parameters = continuousMeasurementEntry.getPerformance().getParameters();
    assertEquals(expectedMu, parameters.getMu());
    assertEquals(expectedSigma, parameters.getSigma());
    assertEquals(ContinuousPerformance.DNORM, continuousMeasurementEntry.getPerformance().getType());
  }

}

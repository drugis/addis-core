package org.drugis.addis.problems;

import org.apache.commons.lang3.tuple.Pair;
import org.drugis.addis.problems.model.*;
import org.drugis.addis.problems.service.impl.PerformanceTableBuilder;
import org.drugis.addis.problems.service.model.*;
import org.drugis.addis.trialverse.model.trialdata.Measurement;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by daan on 3/27/14.
 */
public class PerformanceTableBuilderTest {

  @InjectMocks
  private PerformanceTableBuilder builder;

  String armName1 = "arm name 1";
  String armName2 = "arm name 2";
  Arm arm1 = new Arm(URI.create("1L"), "10L", armName1);
  Arm arm2 = new Arm(URI.create("2L"), "11L", armName2);

  URI alternativeUri1 = URI.create("altUri1");
  URI alternativeUri2 = URI.create("altUri2");
  private URI criterionUri1 = URI.create("critUri1");
  private URI criterionUri2 = URI.create("critUri2");

  String variableName1 = "variable name 1";
  String variableName2 = "variable name 2";
  Variable variable1 = new Variable(URI.create("101L"), "1L", variableName1, "desc", null, false, MeasurementType.RATE, "");
  Variable variable2 = new Variable(URI.create("102L"), "1L", variableName2, "desc", null, false, MeasurementType.CONTINUOUS, "");

  Map<URI, CriterionEntry> criterionEntryMap;
  Map<URI, AlternativeEntry> alternativeEntryMap;

  Measurement measurement1 = new Measurement(URI.create("1L"), variable1.getUri(), arm1.getUri(), 111, 42, null, null);
  Measurement measurement2 = new Measurement(URI.create("1L"), variable2.getUri(), arm1.getUri(), 222, null, 0.2, 7.56);

  CriterionEntry criterionEntry1 = new CriterionEntry(criterionUri1, null, null);
  CriterionEntry criterionEntry2 = new CriterionEntry(criterionUri2, null, null);
  AlternativeEntry alternativeEntry1 = new AlternativeEntry(alternativeUri1, arm1.getName());
  AlternativeEntry alternativeEntry2 = new AlternativeEntry(alternativeUri2, arm2.getName());

  @Before
  public void setUp() throws Exception {
    criterionEntryMap = new HashMap<>();
    criterionEntryMap.put(variable1.getUri(), criterionEntry1);
    criterionEntryMap.put(variable2.getUri(), criterionEntry2);
    alternativeEntryMap = new HashMap<>();
    alternativeEntryMap.put(arm1.getUri(), alternativeEntry1);
    alternativeEntryMap.put(arm2.getUri(), alternativeEntry2);
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
    //criterionUri1, variableName1, alternativeUri1, armName1, mu, stdDev, null, sampleSize1);
    Pair<Measurement, URI> row1 = Pair.of(new Measurement(studyUri, criterionUri1, arm1.getUri(), sampleSize1, null, stdDev, mu), alternativeUri1);
    Pair<Measurement, URI> row2 = Pair.of(new Measurement(studyUri, criterionUri2, arm2.getUri(), sampleSize2, rate, null, null), alternativeUri1);
    Pair<Measurement, URI> row3 = Pair.of(new Measurement(studyUri, criterionUri1, arm2.getUri(), sampleSize2, null, stdDev, mu), alternativeUri1);
    Pair<Measurement, URI> row4 = Pair.of(new Measurement(studyUri, criterionUri2, arm1.getUri(), sampleSize1, rate, null, null), alternativeUri1);

//     row2 = new (criterionUri2, variableName2, alternativeUri2, armName2, null, null, rate, sampleSize2);
//     row3 = new (criterionUri1, variableName1, alternativeUri2, armName2, mu, stdDev, null, sampleSize1);
//     row4 = new (criterionUri2, variableName2, alternativeUri1, armName1, null, null, rate, sampleSize2);

    // EXECUTE
    List<Pair<Measurement, URI>> measurementPairs =Arrays.asList(row1, row2, row3, row4);
    List<AbstractMeasurementEntry> performanceTable = builder.build(measurementPairs);

    assertEquals(4, performanceTable.size());

    ContinuousMeasurementEntry continuousMeasurementEntry = (ContinuousMeasurementEntry) performanceTable.get(0);
    assertEquals(alternativeUri1, continuousMeasurementEntry.getAlternativeUri());
    assertEquals(criterionUri1, continuousMeasurementEntry.getCriterionUri());
    assertEquals(ContinuousPerformance.DNORM, continuousMeasurementEntry.getPerformance().getType());

    RateMeasurementEntry rateMeasurementEntry = (RateMeasurementEntry) performanceTable.get(1);
    assertEquals(alternativeUri2, rateMeasurementEntry.getAlternativeUri());
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

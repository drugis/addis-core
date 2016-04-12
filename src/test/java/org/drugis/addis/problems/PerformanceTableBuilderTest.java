package org.drugis.addis.problems;

import org.drugis.addis.problems.model.*;
import org.drugis.addis.problems.service.impl.PerformanceTableBuilder;
import org.drugis.addis.problems.service.model.*;
import org.drugis.addis.trialverse.model.Measurement;
import org.drugis.addis.trialverse.service.impl.TriplestoreServiceImpl;
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
  Arm arm1 = new Arm("1L", "10L", armName1);
  Arm arm2 = new Arm("2L", "11L", armName2);

  URI alternativeUri1 = URI.create("altUri1");
  URI alternativeUri2 = URI.create("altUri2");
  private String criterionUri1 = "critUri1";
  private String criterionUri2 = "critUri2";

  String variableName1 = "variable name 1";
  String variableName2 = "variable name 2";
  Variable variable1 = new Variable("101L", "1L", variableName1, "desc", null, false, MeasurementType.RATE, "");
  Variable variable2 = new Variable("102L", "1L", variableName2, "desc", null, false, MeasurementType.CONTINUOUS, "");

  Map<String, CriterionEntry> criterionEntryMap;
  Map<String, AlternativeEntry> alternativeEntryMap;

  Measurement measurement1 = new Measurement(URI.create("1L"), variable1.getUid(), arm1.getUid(), 111, 42, null, null);
  Measurement measurement2 = new Measurement(URI.create("1L"), variable2.getUid(), arm1.getUid(), 222, null, 0.2, 7.56);

  CriterionEntry criterionEntry1 = new CriterionEntry(criterionUri1, variable1.getName(), null, null);
  CriterionEntry criterionEntry2 = new CriterionEntry(criterionUri2, variable2.getName(), null, null);
  AlternativeEntry alternativeEntry1 = new AlternativeEntry(alternativeUri1, arm1.getName());
  AlternativeEntry alternativeEntry2 = new AlternativeEntry(alternativeUri2, arm2.getName());

  @Before
  public void setUp() throws Exception {
    criterionEntryMap = new HashMap<>();
    criterionEntryMap.put(variable1.getUid(), criterionEntry1);
    criterionEntryMap.put(variable2.getUid(), criterionEntry2);
    alternativeEntryMap = new HashMap<>();
    alternativeEntryMap.put(arm1.getUid(), alternativeEntry1);
    alternativeEntryMap.put(arm2.getUid(), alternativeEntry2);
    builder = new PerformanceTableBuilder();

    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testBuild() throws Exception {

    Long rate = measurement1.getRate();
    Long sampleSize1 = measurement2.getSampleSize();
    Long alpha = rate + 1L;
    Long beta = sampleSize1 + 1L;

    Long sampleSize2 = measurement1.getSampleSize();
    Double mu = measurement2.getMean();
    Double stdDev = measurement2.getStdDev();

    TriplestoreServiceImpl.SingleStudyBenefitRiskMeasurementRow row1 = new TriplestoreServiceImpl.SingleStudyBenefitRiskMeasurementRow(criterionUri1, variableName1, alternativeUri1, armName1, mu, stdDev, null, sampleSize1);
    TriplestoreServiceImpl.SingleStudyBenefitRiskMeasurementRow row2 = new TriplestoreServiceImpl.SingleStudyBenefitRiskMeasurementRow(criterionUri2, variableName2, alternativeUri2, armName2, null, null, rate, sampleSize2);
    TriplestoreServiceImpl.SingleStudyBenefitRiskMeasurementRow row3 = new TriplestoreServiceImpl.SingleStudyBenefitRiskMeasurementRow(criterionUri1, variableName1, alternativeUri2, armName2, mu, stdDev, null, sampleSize1);
    TriplestoreServiceImpl.SingleStudyBenefitRiskMeasurementRow row4 = new TriplestoreServiceImpl.SingleStudyBenefitRiskMeasurementRow(criterionUri2, variableName2, alternativeUri1, armName1, null, null, rate, sampleSize2);

    List<TriplestoreServiceImpl.SingleStudyBenefitRiskMeasurementRow> measurementRows = Arrays.asList(row1, row2, row3, row4);

    // EXECUTE
    List<AbstractMeasurementEntry> performanceTable = builder.build(measurementRows);

    assertEquals(4, performanceTable.size());

    ContinuousMeasurementEntry continuousMeasurementEntry = (ContinuousMeasurementEntry) performanceTable.get(0);
    assertEquals(alternativeUri1, continuousMeasurementEntry.getAlternativeUri());
    assertEquals(criterionUri1, continuousMeasurementEntry.getCriterionUri());
    assertEquals(ContinuousPerformance.DNORM, continuousMeasurementEntry.getPerformance().getType());

    RateMeasurementEntry rateMeasurementEntry = (RateMeasurementEntry) performanceTable.get(1);
    assertEquals(alternativeUri2, rateMeasurementEntry.getAlternativeUri());
    assertEquals(criterionUri2, rateMeasurementEntry.getCriterionUri());
    assertEquals(RatePerformance.DBETA, rateMeasurementEntry.getPerformance().getType());

    Long expectedAlpha = measurement1.getRate() + 1L;
    Long expectedBeta = measurement1.getSampleSize() - measurement1.getRate() + 1L;
    assertEquals(expectedAlpha, rateMeasurementEntry.getPerformance().getParameters().getAlpha());
    assertEquals(expectedBeta, rateMeasurementEntry.getPerformance().getParameters().getBeta());
    assertEquals(RatePerformance.DBETA, rateMeasurementEntry.getPerformance().getType());

    Double expectedMu = measurement2.getMean();
    Long expectedSampleSize = measurement2.getSampleSize();
    Double expectedSigma = measurement2.getStdDev() / Math.sqrt(expectedSampleSize);

    ContinuousPerformanceParameters parameters = continuousMeasurementEntry.getPerformance().getParameters();
    assertEquals(expectedMu, parameters.getMu());
    assertEquals(expectedSigma, parameters.getSigma());
    assertEquals(ContinuousPerformance.DNORM, continuousMeasurementEntry.getPerformance().getType());
  }

}

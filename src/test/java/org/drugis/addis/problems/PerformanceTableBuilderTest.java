package org.drugis.addis.problems;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.drugis.addis.problems.model.*;
import org.drugis.addis.problems.service.impl.PerformanceTableBuilder;
import org.drugis.addis.problems.service.model.*;
import org.drugis.addis.util.JSONUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Created by daan on 3/27/14.
 */
public class PerformanceTableBuilderTest {

  @Mock
  JSONUtils jsonUtils;

  @InjectMocks
  private PerformanceTableBuilder builder;

  String armName1 = "arm name 1";
  String armName2 = "arm name 2";
  Arm arm1 = new Arm(1L, 10L, armName1);
  Arm arm2 = new Arm(2L, 11L, armName2);

  String variableName1 = "variable name 1";
  String variableName2 = "variable name 2";
  Variable variable1 = new Variable(101L, 1L, variableName1, "desc", null, false, MeasurementType.RATE, "");
  Variable variable2 = new Variable(102L, 1L, variableName2, "desc", null, false, MeasurementType.CONTINUOUS, "");

  Map<Long, CriterionEntry> criterionEntryMap;
  Map<Long, AlternativeEntry> alternativeEntryMap;

  Long measurementMomentId = 1L;

  Measurement measurement1 = new Measurement(1L, variable1.getId(), measurementMomentId, arm1.getId(), MeasurementAttribute.RATE, 42L, null);
  Measurement measurement2 = new Measurement(1L, variable1.getId(), measurementMomentId, arm1.getId(), MeasurementAttribute.SAMPLE_SIZE, 68L, null);
  Measurement measurement3 = new Measurement(1L, variable2.getId(), measurementMomentId, arm1.getId(), MeasurementAttribute.MEAN, null, 7.56);
  Measurement measurement4 = new Measurement(1L, variable2.getId(), measurementMomentId, arm1.getId(), MeasurementAttribute.SAMPLE_SIZE, 44L, null);
  Measurement measurement5 = new Measurement(1L, variable2.getId(), measurementMomentId, arm1.getId(), MeasurementAttribute.STANDARD_DEVIATION, null, 2.1);

  CriterionEntry criterionEntry1 = new CriterionEntry(variable1.getName(), null, null);
  CriterionEntry criterionEntry2 = new CriterionEntry(variable2.getName(), null, null);
  AlternativeEntry alternativeEntry1 = new AlternativeEntry(arm1.getName());
  AlternativeEntry alternativeEntry2 = new AlternativeEntry(arm2.getName());
  Pair<AlternativeEntry, CriterionEntry> performance1Key = new ImmutablePair<>(alternativeEntry1, criterionEntry1);
  Pair<AlternativeEntry, CriterionEntry> performance2Key = new ImmutablePair<>(alternativeEntry1, criterionEntry2);

  List<Measurement> measurements = Arrays.asList(measurement1, measurement2, measurement3, measurement4, measurement5);

  @Before
  public void setUp() throws Exception {
    criterionEntryMap = new HashMap<>();
    criterionEntryMap.put(variable1.getId(), criterionEntry1);
    criterionEntryMap.put(variable2.getId(), criterionEntry2);
    alternativeEntryMap = new HashMap<>();
    alternativeEntryMap.put(arm1.getId(), alternativeEntry1);
    alternativeEntryMap.put(arm2.getId(), alternativeEntry2);
    builder = new PerformanceTableBuilder();

    jsonUtils = new JSONUtils();
    MockitoAnnotations.initMocks(this);



  }

  @Test
  public void testCreatePerformanceMap() throws Exception {
    // execution
    Map<Pair<AlternativeEntry, CriterionEntry>, Map<MeasurementAttribute, Measurement>> performanceMap = builder.createPerformanceMap(criterionEntryMap, alternativeEntryMap, measurements);

    assertEquals(2, performanceMap.size());
    assertEquals(2, performanceMap.get(performance1Key).size());
    assertEquals(3, performanceMap.get(performance2Key).size());
    assertEquals(measurement1, performanceMap.get(performance1Key).get(MeasurementAttribute.RATE));
  }

  @Test
  public void testBuild() throws Exception {
    when(jsonUtils.createKey(armName1)).thenReturn(armName1);
    when(jsonUtils.createKey(armName2)).thenReturn(armName2);
    when(jsonUtils.createKey(variableName1)).thenReturn(variableName1);
    when(jsonUtils.createKey(variableName2)).thenReturn(variableName2);

    List<AbstractMeasurementEntry> performanceTable = builder.build(criterionEntryMap, alternativeEntryMap, measurements);

    assertEquals(2, performanceTable.size());
    RateMeasurementEntry rateMeasurementEntry = (RateMeasurementEntry) performanceTable.get(0);
    assertEquals(armName1, rateMeasurementEntry.getAlternative());
    assertEquals(variableName1, rateMeasurementEntry.getCriterion());
    assertEquals(RatePerformance.DBETA, rateMeasurementEntry.getPerformance().getType());
    ContinuousMeasurementEntry continuousMeasurementEntry = (ContinuousMeasurementEntry) performanceTable.get(1);
    assertEquals(armName1, continuousMeasurementEntry.getAlternative());
    assertEquals(variableName2, continuousMeasurementEntry.getCriterion());
    assertEquals(ContinuousPerformance.DNORM, continuousMeasurementEntry.getPerformance().getType());
  }

  @Test
  public void testCreateBetaDistributionEntry() throws Exception {
    Measurement rateMeasurement = measurement1;
    Measurement sampleSizeMeasurement = measurement2;

    Map<MeasurementAttribute, Measurement> measurementMap = new HashMap<>();
    measurementMap.put(MeasurementAttribute.RATE, rateMeasurement);
    measurementMap.put(MeasurementAttribute.SAMPLE_SIZE, sampleSizeMeasurement);

    // EXECUTOR
    RateMeasurementEntry entry = builder.createBetaDistributionEntry(alternativeEntry1, criterionEntry1, measurementMap);

    Long expectedAlpha = rateMeasurement.getIntegerValue() + 1L;
    Long expectedBeta = sampleSizeMeasurement.getIntegerValue() - rateMeasurement.getIntegerValue() + 1L;
    assertEquals(expectedAlpha, entry.getPerformance().getParameters().getAlpha());
    assertEquals(expectedBeta, entry.getPerformance().getParameters().getBeta());
    assertEquals(RatePerformance.DBETA, entry.getPerformance().getType());
  }

  @Test
  public void testCreateNormalDistributionEntry() {
    Measurement meanMeasurement = measurement3;
    Measurement sampleSizeMeasurement = measurement4;
    Measurement standardDeviationMeasurement = measurement5;

    Map<MeasurementAttribute, Measurement> measurementMap = new HashMap<>();
    measurementMap.put(MeasurementAttribute.MEAN, meanMeasurement);
    measurementMap.put(MeasurementAttribute.SAMPLE_SIZE, sampleSizeMeasurement);
    measurementMap.put(MeasurementAttribute.STANDARD_DEVIATION, standardDeviationMeasurement);

    // EXECUTOR
    ContinuousMeasurementEntry entry = builder.createNormalDistributionEntry(alternativeEntry1, criterionEntry1, measurementMap);

    Double expectedMu = meanMeasurement.getRealValue();
    Long sampleSize = sampleSizeMeasurement.getIntegerValue();
    Double expectedSigma = standardDeviationMeasurement.getRealValue() / Math.sqrt(sampleSize);

    ContinuousPerformanceParameters parameters = entry.getPerformance().getParameters();
    assertEquals(expectedMu, parameters.getMu());
    assertEquals(expectedSigma, parameters.getSigma());
    assertEquals(ContinuousPerformance.DNORM, entry.getPerformance().getType());
  }
}

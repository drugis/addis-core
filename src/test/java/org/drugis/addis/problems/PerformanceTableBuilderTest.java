package org.drugis.addis.problems;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.drugis.addis.problems.model.*;
import org.drugis.addis.problems.service.impl.PerformanceTableBuilder;
import org.drugis.addis.problems.service.model.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

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

  String alternativeUri1 = "altUri1";
  String alternativeUri2 = "altUri2";
  private String criterionUri1 = "critUri1";
  private String criterionUri2 = "critUri2";

  String variableName1 = "variable name 1";
  String variableName2 = "variable name 2";
  Variable variable1 = new Variable("101L", "1L", variableName1, "desc", null, false, MeasurementType.RATE, "");
  Variable variable2 = new Variable("102L", "1L", variableName2, "desc", null, false, MeasurementType.CONTINUOUS, "");

  Map<String, CriterionEntry> criterionEntryMap;
  Map<String, AlternativeEntry> alternativeEntryMap;

  Measurement measurement1 = new Measurement("1L", variable1.getUid(), arm1.getUid(), 111L, 42L, null, null);
  Measurement measurement2 = new Measurement("1L", variable2.getUid(), arm1.getUid(), 222L, null, 0.2, 7.56);

  CriterionEntry criterionEntry1 = new CriterionEntry(criterionUri1, variable1.getName(), null, null);
  CriterionEntry criterionEntry2 = new CriterionEntry(criterionUri2, variable2.getName(), null, null);
  AlternativeEntry alternativeEntry1 = new AlternativeEntry(alternativeUri1, arm1.getName());
  AlternativeEntry alternativeEntry2 = new AlternativeEntry(alternativeUri2, arm2.getName());
  Pair<AlternativeEntry, CriterionEntry> performance1Key = new ImmutablePair<>(alternativeEntry1, criterionEntry1);
  Pair<AlternativeEntry, CriterionEntry> performance2Key = new ImmutablePair<>(alternativeEntry1, criterionEntry2);

  List<Measurement> measurements = Arrays.asList(measurement1, measurement2);

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
  public void testCreatePerformanceMap() throws Exception {
    Map<Pair<AlternativeEntry, CriterionEntry>, Measurement> performanceMap = builder.createPerformanceMap(criterionEntryMap, alternativeEntryMap, measurements);
    assertEquals(2, performanceMap.size());
  }

  @Test
  public void testBuild() throws Exception {
    List<AbstractMeasurementEntry> performanceTable = builder.build(criterionEntryMap, alternativeEntryMap, measurements);

    assertEquals(2, performanceTable.size());
    RateMeasurementEntry rateMeasurementEntry = (RateMeasurementEntry) performanceTable.get(0);
    assertEquals(alternativeUri1, rateMeasurementEntry.getAlternativeUri());
    assertEquals(criterionUri1, rateMeasurementEntry.getCriterionUri());
    assertEquals(RatePerformance.DBETA, rateMeasurementEntry.getPerformance().getType());
    ContinuousMeasurementEntry continuousMeasurementEntry = (ContinuousMeasurementEntry) performanceTable.get(1);
    assertEquals(alternativeUri1, continuousMeasurementEntry.getAlternativeUri());
    assertEquals(criterionUri2, continuousMeasurementEntry.getCriterionUri());
    assertEquals(ContinuousPerformance.DNORM, continuousMeasurementEntry.getPerformance().getType());
  }

  @Test
  public void testCreateBetaDistributionEntry() throws Exception {

    // EXECUTOR
    RateMeasurementEntry entry = builder.createBetaDistributionEntry(alternativeEntry1, criterionEntry1, measurement1);

    Long expectedAlpha = measurement1.getRate() + 1L;
    Long expectedBeta = measurement1.getSampleSize() - measurement1.getRate() + 1L;
    assertEquals(expectedAlpha, entry.getPerformance().getParameters().getAlpha());
    assertEquals(expectedBeta, entry.getPerformance().getParameters().getBeta());
    assertEquals(RatePerformance.DBETA, entry.getPerformance().getType());
  }

  @Test
  public void testCreateNormalDistributionEntry() {

    // EXECUTOR
    ContinuousMeasurementEntry entry = builder.createNormalDistributionEntry(alternativeEntry1, criterionEntry1, measurement2);

    Double expectedMu = measurement2.getMean();
    Long sampleSize = measurement2.getSampleSize();
    Double expectedSigma = measurement2.getStdDev() / Math.sqrt(sampleSize);

    ContinuousPerformanceParameters parameters = entry.getPerformance().getParameters();
    assertEquals(expectedMu, parameters.getMu());
    assertEquals(expectedSigma, parameters.getSigma());
    assertEquals(ContinuousPerformance.DNORM, entry.getPerformance().getType());
  }
}

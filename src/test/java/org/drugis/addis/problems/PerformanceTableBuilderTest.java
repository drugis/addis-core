package org.drugis.addis.problems;

import org.drugis.addis.problems.model.*;
import org.drugis.addis.problems.service.impl.PerformanceTableBuilder;
import org.drugis.addis.problems.service.model.AbstractMeasurementEntry;
import org.drugis.addis.problems.service.model.ContinuousMeasurementEntry;
import org.drugis.addis.problems.service.model.RateMeasurementEntry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by daan on 3/27/14.
 */
public class PerformanceTableBuilderTest {
  private PerformanceTableBuilder builder;

  Arm arm1 = new Arm(1L, "arm name 1");
  Arm arm2 = new Arm(2L, "arm name 2");

  Variable variable1 = new Variable(101L, 1L, "variable name 1", "desc", null, false, MeasurementType.RATE, "");
  Variable variable2 = new Variable(102L, 1L, "variable name 2", "desc", null, false, MeasurementType.CONTINUOUS, "");

  Long measurementMomentId = 1L;

  Measurement measurement1 = new Measurement(1L, variable1.getId(), measurementMomentId, arm1.getId(), MeasurementAttribute.RATE, 42L, null);
  Measurement measurement2 = new Measurement(1L, variable1.getId(), measurementMomentId, arm1.getId(), MeasurementAttribute.SAMPLE_SIZE, 68L, null);
  Measurement measurement3 = new Measurement(1L, variable2.getId(), measurementMomentId, arm1.getId(), MeasurementAttribute.MEAN, null, 7.56);
  Measurement measurement4 = new Measurement(1L, variable2.getId(), measurementMomentId, arm1.getId(), MeasurementAttribute.SAMPLE_SIZE, 44L, null);
  Measurement measurement5 = new Measurement(1L, variable2.getId(), measurementMomentId, arm1.getId(), MeasurementAttribute.STANDARD_DEVIATION, null, 2.1);

  @Before
  public void setUp() throws Exception {
    List<Variable> variables = Arrays.asList(variable1, variable2);
    List<Arm> arms = Arrays.asList(arm1, arm2);
    List<Measurement> measurements = Arrays.asList(measurement1, measurement2, measurement3, measurement4, measurement5);
    builder = new PerformanceTableBuilder(variables, arms, measurements);
  }

  @Test
  public void testCreatePerformanceMap() throws Exception {

    // execution
    Map<Map<Arm, Variable>, Map<MeasurementAttribute, Measurement>> performanceMap = builder.createPerformanceMap();

    assertEquals(2, performanceMap.size());
    Map<Arm, Variable> performance1Key = new HashMap<>();
    performance1Key.put(arm1, variable1);
    Map<Arm, Variable> performance2Key = new HashMap<>();
    performance2Key.put(arm1, variable2);
    assertEquals(2, performanceMap.get(performance1Key).size());
    assertEquals(3, performanceMap.get(performance2Key).size());
    assertEquals(measurement1, performanceMap.get(performance1Key).get(MeasurementAttribute.RATE));
  }

  @Test
  public void testBuild() throws Exception {
    List<AbstractMeasurementEntry> performanceTable = builder.build();
    assertEquals(2, performanceTable.size());
  }

  @Test
  public void testCreateBetaDistributionEntry() throws Exception {
    Measurement rateMeasurement = measurement1;
    Measurement sampleSizeMeasurement = measurement2;

    Map<MeasurementAttribute, Measurement> measurementMap = new HashMap<>();
    measurementMap.put(MeasurementAttribute.RATE, rateMeasurement);
    measurementMap.put(MeasurementAttribute.SAMPLE_SIZE, sampleSizeMeasurement);

    // EXECUTOR
    RateMeasurementEntry entry = builder.createBetaDistributionEntry(measurementMap);

    Long expectedAlpha = rateMeasurement.getIntegerValue() + 1L;
    Long expectedBeta = sampleSizeMeasurement.getIntegerValue() - rateMeasurement.getIntegerValue() + 1L;
    assertEquals(expectedAlpha, entry.getPerformance().getParameters().getAlpha());
    assertEquals(expectedBeta, entry.getPerformance().getParameters().getBeta());
    assertEquals("dbeta", entry.getPerformance().getType());
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
    ContinuousMeasurementEntry entry = builder.createNormalDistributionEntry(measurementMap);

    Double expectedMu = meanMeasurement.getRealValue();
    Double expectedSigma = standardDeviationMeasurement.getRealValue();
    assertEquals(expectedMu, entry.getPerformance().getParameters().getMu());
    assertEquals(expectedSigma, entry.getPerformance().getParameters().getSigma());
    assertEquals("dnormal", entry.getPerformance().getType());
  }
}

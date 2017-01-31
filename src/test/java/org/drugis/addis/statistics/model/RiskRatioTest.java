package org.drugis.addis.statistics.model;

/**
 * Created by joris on 25-1-17.
 */

import org.drugis.addis.statistics.command.DichotomousMeasurementCommand;
import org.drugis.addis.statistics.exception.MissingMeasurementException;
import org.junit.Before;
import org.junit.Test;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class RiskRatioTest {
  private DichotomousMeasurementCommand baseline;
  private DichotomousMeasurementCommand subject;
  private RiskRatio ratio;

  private static final Integer subjectSize = 142;
  private static final Integer baselineSize = 144;
  private static final URI ENDPOINT_1_URI = URI.create("http://endpoint.com/1");
  private static final URI ARM_1_URI = URI.create("http://arm.org/1");
  private static final URI ARM_2_URI = URI.create("http://arm.org/2");

  @Before
  public void setUp() {
    Integer countE1A1 = 73;
    Integer countE1A2 = 63;
    Map<String, Double> measurement1Properties = new HashMap<>();
    measurement1Properties.put("count", Double.valueOf(countE1A1));
    measurement1Properties.put("sampleSize", Double.valueOf(subjectSize));
    Map<String, Double> measurement2Properties = new HashMap<>();
    measurement2Properties.put("count", Double.valueOf(countE1A2));
    measurement2Properties.put("sampleSize", Double.valueOf(baselineSize));
    subject = new DichotomousMeasurementCommand(ENDPOINT_1_URI, ARM_1_URI, measurement1Properties);
    baseline = new DichotomousMeasurementCommand(ENDPOINT_1_URI, ARM_2_URI, measurement2Properties);
    ratio = new RiskRatio(baseline, subject);
  }

  @Test
  public void testGetMean() throws MissingMeasurementException {
    assertEquals(1.18, ratio.getDistribution().getQuantile(0.5), 0.01);
  }

  @Test
  public void testGetConfidenceInterval() throws MissingMeasurementException {
    assertEquals(0.92, (ratio.getDistribution().getQuantile(0.025)), 0.01);
    assertEquals(1.50, (ratio.getDistribution().getQuantile(0.975)), 0.01);
  }

  @Test
  public void testGetSampleSize() throws MissingMeasurementException {
    int expected = subjectSize + baselineSize;
    assertEquals(expected, (int) ratio.getSampleSize());
  }

  @Test
  public void testPValue() throws MissingMeasurementException {
    double prob = ratio.getDistribution().getCumulativeProbability(ratio.getNeutralValue());
    assertEquals(0.197357423294787, 2 * Math.min(prob, 1 - prob), 0.0000001);
  }

  @Test
  public void testZeroBaselineRateShouldBeUndefined() throws MissingMeasurementException {
    Map<String, Double> measurement1Properties = new HashMap<>();
    measurement1Properties.put("count", 0d);
    measurement1Properties.put("sampleSize", 100d);
    Map<String, Double> measurement2Properties = new HashMap<>();
    measurement2Properties.put("count", 50d);
    measurement2Properties.put("sampleSize", 100d);
    baseline = new DichotomousMeasurementCommand(ENDPOINT_1_URI, ARM_2_URI, measurement1Properties);
    subject = new DichotomousMeasurementCommand(ENDPOINT_1_URI, ARM_1_URI, measurement2Properties);
    ratio = new RiskRatio(baseline, subject);
    assertFalse(ratio.isDefined());
  }

  @Test
  public void testFullSubjectRateShouldBeDefined() throws MissingMeasurementException {
    Map<String, Double> measurement1Properties = new HashMap<>();
    measurement1Properties.put("count", 50d);
    measurement1Properties.put("sampleSize", 100d);
    Map<String, Double> measurement2Properties = new HashMap<>();
    measurement2Properties.put("count", 100d);
    measurement2Properties.put("sampleSize", 100d);
    baseline = new DichotomousMeasurementCommand(ENDPOINT_1_URI, ARM_2_URI, measurement1Properties);
    subject = new DichotomousMeasurementCommand(ENDPOINT_1_URI, ARM_1_URI, measurement2Properties);
    ratio = new RiskRatio(baseline, subject);
    assertTrue(ratio.isDefined());
  }

  @Test
  public void testZeroSubjectRateShouldBeUndefined() throws MissingMeasurementException { // although we can calculate a point-estimate, we can't get a CI.
    Map<String, Double> measurement1Properties = new HashMap<>();
    measurement1Properties.put("count", 50d);
    measurement1Properties.put("sampleSize", 100d);
    Map<String, Double> measurement2Properties = new HashMap<>();
    measurement2Properties.put("count", 0d);
    measurement2Properties.put("sampleSize", 100d);
    baseline = new DichotomousMeasurementCommand(ENDPOINT_1_URI, ARM_2_URI, measurement1Properties);
    subject = new DichotomousMeasurementCommand(ENDPOINT_1_URI, ARM_1_URI, measurement2Properties);
    ratio = new RiskRatio(baseline, subject);
    assertFalse(ratio.isDefined());
  }

  @Test
  public void testFullBaselineRateShouldBeDefined() throws MissingMeasurementException {
    Map<String, Double> measurement1Properties = new HashMap<>();
    measurement1Properties.put("count", 100d);
    measurement1Properties.put("sampleSize", 100d);
    Map<String, Double> measurement2Properties = new HashMap<>();
    measurement2Properties.put("count", 50d);
    measurement2Properties.put("sampleSize", 100d);
    baseline = new DichotomousMeasurementCommand(ENDPOINT_1_URI, ARM_2_URI, measurement1Properties);
    subject = new DichotomousMeasurementCommand(ENDPOINT_1_URI, ARM_1_URI, measurement2Properties);
    ratio = new RiskRatio(baseline, subject);
    assertTrue(ratio.isDefined());
  }

  @Test (expected = IllegalArgumentException.class)
  public void testUndefinedShouldResultInNaN() throws MissingMeasurementException {
    Map<String, Double> measurement1Properties = new HashMap<>();
    measurement1Properties.put("count", 0d);
    measurement1Properties.put("sampleSize", 100d);
    Map<String, Double> measurement2Properties = new HashMap<>();
    measurement2Properties.put("count", 50d);
    measurement2Properties.put("sampleSize", 100d);
    baseline = new DichotomousMeasurementCommand(ENDPOINT_1_URI, ARM_2_URI, measurement1Properties);
    subject = new DichotomousMeasurementCommand(ENDPOINT_1_URI, ARM_1_URI, measurement2Properties);
    ratio = new RiskRatio(baseline, subject);
    assertEquals(Double.NaN, ratio.getSigma(), 0.001);
    assertEquals(Double.NaN, ratio.getDistribution().getQuantile(0.5), 0.001);
  }
}
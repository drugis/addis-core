package org.drugis.addis.statistics.model;

/**
 * Created by joris on 25-1-17.
 */

import org.drugis.addis.statistics.command.DichotomousMeasurementCommand;
import org.junit.Before;
import org.junit.Test;
import java.net.URI;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class RiskRatioTest {
  private DichotomousMeasurementCommand baseline;
  private DichotomousMeasurementCommand subject;
  private RiskRatio ratio;

  private Integer countE1A1 = 73;
  private Integer countE1A2 = 63;
  private static final int subjectSize = 142;
  private static final int baselineSize = 144;
  public static final URI ENDPOINT_1_URI = URI.create("http://endpoint.com/1");
  public static final URI ARM_1_URI = URI.create("http://arm.org/1");
  public static final URI ARM_2_URI = URI.create("http://arm.org/2");

  @Before
  public void setUp() {
    subject = new DichotomousMeasurementCommand(ENDPOINT_1_URI, ARM_1_URI, countE1A1, subjectSize);
    baseline = new DichotomousMeasurementCommand(ENDPOINT_1_URI, ARM_2_URI, countE1A2, baselineSize);
    ratio = new RiskRatio(baseline, subject);
  }

  @Test
  public void testGetMean() {
    assertEquals(1.18, ratio.getDistribution().getQuantile(0.5), 0.01);
  }

  @Test
  public void testGetConfidenceInterval() {
    assertEquals(0.92, (ratio.getDistribution().getQuantile(0.025)), 0.01);
    assertEquals(1.50, (ratio.getDistribution().getQuantile(0.975)), 0.01);
  }

  @Test
  public void testGetSampleSize() {
    int expected = subjectSize + baselineSize;
    assertEquals(expected, (int) ratio.getSampleSize());
  }

  @Test
  public void testPValue() {
    double prob = ratio.getDistribution().getCumulativeProbability(ratio.getNeutralValue());
    assertEquals(0.197357423294787, 2 * Math.min(prob, 1 - prob), 0.0000001);
  }

  @Test
  public void testZeroBaselineRateShouldBeUndefined() {
    baseline = new DichotomousMeasurementCommand(ENDPOINT_1_URI, ARM_2_URI, 0, 100);
    subject = new DichotomousMeasurementCommand(ENDPOINT_1_URI, ARM_1_URI, 50, 100);
    ratio = new RiskRatio(baseline, subject);
    assertFalse(ratio.isDefined());
  }

  @Test
  public void testFullSubjectRateShouldBeDefined() {
    baseline = new DichotomousMeasurementCommand(ENDPOINT_1_URI, ARM_2_URI, 50, 100);
    subject = new DichotomousMeasurementCommand(ENDPOINT_1_URI, ARM_1_URI, 100, 100);
    ratio = new RiskRatio(baseline, subject);
    assertTrue(ratio.isDefined());
  }

  @Test
  public void testZeroSubjectRateShouldBeUndefined() { // although we can calculate a point-estimate, we can't get a CI.
    baseline = new DichotomousMeasurementCommand(ENDPOINT_1_URI, ARM_2_URI, 50, 100);
    subject = new DichotomousMeasurementCommand(ENDPOINT_1_URI, ARM_1_URI, 0, 100);
    ratio = new RiskRatio(baseline, subject);
    assertFalse(ratio.isDefined());
  }

  @Test
  public void testFullBaselineRateShouldBeDefined() {
    baseline = new DichotomousMeasurementCommand(ENDPOINT_1_URI, ARM_2_URI, 100, 100);
    subject = new DichotomousMeasurementCommand(ENDPOINT_1_URI, ARM_1_URI, 50, 100);
    ratio = new RiskRatio(baseline, subject);
    assertTrue(ratio.isDefined());
  }

  @Test (expected = IllegalArgumentException.class)
  public void testUndefinedShouldResultInNaN() {
    baseline = new DichotomousMeasurementCommand(ENDPOINT_1_URI, ARM_2_URI, 0, 100);
    subject = new DichotomousMeasurementCommand(ENDPOINT_1_URI, ARM_1_URI, 50, 100);
    ratio = new RiskRatio(baseline, subject);
    assertEquals(Double.NaN, ratio.getSigma(), 0.001);
    assertEquals(Double.NaN, ratio.getDistribution().getQuantile(0.5), 0.001);
  }
}
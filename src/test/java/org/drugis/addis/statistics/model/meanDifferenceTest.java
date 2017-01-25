package org.drugis.addis.statistics.model;

import org.apache.commons.math3.distribution.TDistribution;
import org.drugis.addis.statistics.command.ContinuousMeasurementCommand;
import org.junit.Before;
import org.junit.Test;
import java.net.URI;
import static junit.framework.TestCase.assertEquals;

/**
 * Created by joris on 25-1-17.
 */
public class meanDifferenceTest {
  private Double meanE1A1 = 0.2342;
  private Double stdDevE1A1 = 0.2;
  private Double meanE1A2 = 4.7811;
  private Double stdDevE1A2 = 2.5;
  private static final int subjectSize = 35;
  private static final int baselineSize = 41;
  private MeanDifference meanDifference;
  private ContinuousMeasurementCommand subject;
  private ContinuousMeasurementCommand baseline;
  public static final URI ENDPOINT_1_URI = URI.create("http://endpoint.com/1");
  public static final URI ARM_1_URI = URI.create("http://arm.org/1");
  public static final URI ARM_2_URI = URI.create("http://arm.org/2");

  @Before
  public void setUp() {
    subject = new ContinuousMeasurementCommand(ENDPOINT_1_URI, ARM_1_URI, meanE1A1, stdDevE1A1, subjectSize);
    baseline = new ContinuousMeasurementCommand(ENDPOINT_1_URI, ARM_2_URI, meanE1A2, stdDevE1A2, baselineSize);
    meanDifference = new MeanDifference(baseline, subject);
  }

  @Test
  public void testGetMean() {
    assertEquals(meanE1A1 - meanE1A2, meanDifference.getDistribution().getQuantile(0.5), 0.0001);
  }

  @Test
  public void testGetError() {
    Double expected = Math.sqrt(square(stdDevE1A1) / subjectSize + square(stdDevE1A2) / baselineSize);
    assertEquals(expected, meanDifference.getSigma(), 0.0001);
  }

  @Test
  public void testGetConfidenceInterval() {
    double t = getT(subjectSize + baselineSize - 2);
    double upper = meanDifference.getDistribution().getQuantile(0.5) + t * meanDifference.getSigma();
    double lower = meanDifference.getDistribution().getQuantile(0.5) - t * meanDifference.getSigma();
    assertEquals(upper, meanDifference.getDistribution().getQuantile(0.975), 0.0001);
    assertEquals(lower, meanDifference.getDistribution().getQuantile(0.025), 0.0001);
  }

  @Test
  public void testGetSampleSize() {
    int expected = subjectSize + baselineSize;
    assertEquals(expected, (int) meanDifference.getSampleSize());
  }

  @Test
  public void testPValue() {
    double prob = meanDifference.getDistribution().getCumulativeProbability(meanDifference.getNeutralValue());
    assertEquals(0.0, 2 * Math.min(prob, 1 - prob), 0.0000001);
  }

  private Double square(Double x) {
    return x * x;
  }

  private  Double getT(int v) {
    TDistribution dist = new TDistribution(v);
    return dist.inverseCumulativeProbability(0.975);
  }
}

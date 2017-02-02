package org.drugis.addis.statistics.model;

/**
 * Created by joris on 25-1-17.
 */

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class TransformedStudentTTest {

  private TransformedStudentT student1;
  private TransformedStudentT student2;

  @Before
  public void setUp() {
    student1 = new TransformedStudentT(0.0, 1.0, 1);
    student2 = new TransformedStudentT(-5.0, 2.0, 9);
  }

  @Test
  public void testGetParameters() {
    assertEquals(0.0, student1.getMu(), 0.00000001);
    assertEquals(1.0, student1.getSigma(), 0.00000001);
    assertEquals(-5.0, student2.getMu(), 0.00000001);
    assertEquals(2.0, student2.getSigma(), 0.00000001);
  }

  @Test
  public void testGetQuantile() {
    double t1_90 = 6.314;
    double t1_95 = 12.706;
    double t9_90 = 1.833;
    double t9_95 = 2.262;
    assertEquals(t1_95 * 1.0 + 0.0, student1.getQuantile(0.975), 0.001);
    assertEquals(t9_95 * 2.0 + -5.0, student2.getQuantile(0.975), 0.001);
    assertEquals(-t1_95 * 1.0 + 0.0, student1.getQuantile(0.025), 0.001);
    assertEquals(-t9_95 * 2.0 + -5.0, student2.getQuantile(0.025), 0.001);
    assertEquals(t1_90 * 1.0 + 0.0, student1.getQuantile(0.95), 0.001);
    assertEquals(t9_90 * 2.0 + -5.0, student2.getQuantile(0.95), 0.001);
    assertEquals(0.0, student1.getQuantile(0.5), 0.00001);
    assertEquals(-5.0, student2.getQuantile(0.5), 0.00001);
  }

  @Test
  public void testCalculateCumulativeProbability() {
    assertEquals(0.5, student1.calculateCumulativeProbability(student1.getMu()), 0.000001);
    assertEquals(0.75, student1.calculateCumulativeProbability(student1.getSigma()), 0.000001);
    assertEquals(0.25, student1.calculateCumulativeProbability(-student1.getSigma()), 0.000001);
    assertEquals(0.5, student2.calculateCumulativeProbability(student2.getMu()), 0.000001);
    assertEquals(0.8282818, student2.calculateCumulativeProbability(student2.getMu() + student2.getSigma()), 0.000001);
    assertEquals(0.1717181, student2.calculateCumulativeProbability(student2.getMu() - student2.getSigma()), 0.000001);
  }


  @Test(expected = IllegalArgumentException.class)
  public void testPreconditionSigmaNonNegative() {
    new TransformedStudentT(0.0, -.01, 1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPreconditionSigmaNotNaN() {
    new TransformedStudentT(0.0, Double.NaN, 1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPreconditionMuNotNaN() {
    new TransformedStudentT(Double.NaN, 1.0, 1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPreconditionDegreesOfFreedomPositive() {
    new TransformedStudentT(0.0, 1.0, 0);
  }
}


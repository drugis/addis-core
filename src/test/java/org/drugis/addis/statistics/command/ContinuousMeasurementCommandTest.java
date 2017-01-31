package org.drugis.addis.statistics.command;

import org.drugis.addis.statistics.exception.MissingMeasurementException;
import org.junit.Test;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by joris on 31-1-17.
 */
public class ContinuousMeasurementCommandTest {
  private static final URI ENDPOINT_1_URI = URI.create("http://endpoint.com/1");
  private static final URI ARM_1_URI = URI.create("http://arm.org/1");

  @Test
  public void testGetStdDev() throws MissingMeasurementException {
    Map<String, Double> resultProperties = new HashMap<>();
    resultProperties.put("standardDeviation", 3.4);
    ContinuousMeasurementCommand commandWithStdDev = new ContinuousMeasurementCommand(ENDPOINT_1_URI, ARM_1_URI, resultProperties);
    assertEquals(3.4, commandWithStdDev.getStdDev(), 0.00001);
  }

  @Test
  public void getStdDevFromStdErr() throws MissingMeasurementException {
    Map<String, Double> resultProperties = new HashMap<>();
    resultProperties.put("standardError", 3.3);
    resultProperties.put("sampleSize", 9.0);
    ContinuousMeasurementCommand commandWithStdErr = new ContinuousMeasurementCommand(ENDPOINT_1_URI, ARM_1_URI, resultProperties);
    assertEquals(9.9, commandWithStdErr.getStdDev(), 0.00001);
  }

  @Test(expected = MissingMeasurementException.class)
  public void getFromEmpty() throws MissingMeasurementException {
    ContinuousMeasurementCommand emptyCommand = new ContinuousMeasurementCommand(ENDPOINT_1_URI, ARM_1_URI, new HashMap<>());
    emptyCommand.getStdDev();
  }

  @Test(expected = MissingMeasurementException.class)
  public void getWitMissingStdDev() throws MissingMeasurementException {
    Map<String, Double> resultProperties = new HashMap<>();
    resultProperties.put("sampleSize", 9.0);
    ContinuousMeasurementCommand commandWithoutStdDev = new ContinuousMeasurementCommand(ENDPOINT_1_URI, ARM_1_URI, resultProperties);
    commandWithoutStdDev.getStdDev();
  }

}
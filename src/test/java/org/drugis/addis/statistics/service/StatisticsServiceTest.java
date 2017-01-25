package org.drugis.addis.statistics.service;

import org.drugis.addis.statistics.command.AbstractMeasurementCommand;
import org.drugis.addis.statistics.command.ContinuousMeasurementCommand;
import org.drugis.addis.statistics.command.DichotomousMeasurementCommand;
import org.drugis.addis.statistics.command.EstimatesCommand;
import org.drugis.addis.statistics.model.Estimate;
import org.drugis.addis.statistics.model.Estimates;
import org.drugis.addis.statistics.service.impl.StatisticsServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by joris on 24-1-17.
 */
public class StatisticsServiceTest {

  @InjectMocks
  StatisticsService statisticsService;
  public static final URI ENDPOINT_1_URI = URI.create("http://endpoint.com/1");
  public static final URI ARM_1_URI = URI.create("http://arm.org/1");
  public static final URI ARM_2_URI = URI.create("http://arm.org/2");
  public static final Integer SAMPLE_SIZE_E_1_A_1 = 20;
  public static final Integer SAMPLE_SIZE_E_1_A_2 = 21;

  @Before
  public void startUp() {

    statisticsService = new StatisticsServiceImpl();
    initMocks(this);
  }

  @Test
  public void testGetEstimatesForDichotomous() {
    Integer countE1A1 = 10;
    Integer countE1A2 = 11;
    AbstractMeasurementCommand measurementE1A1 = new DichotomousMeasurementCommand(ENDPOINT_1_URI, ARM_1_URI, countE1A1, SAMPLE_SIZE_E_1_A_1);
    AbstractMeasurementCommand measurementE1A2 = new DichotomousMeasurementCommand(ENDPOINT_1_URI, ARM_2_URI, countE1A2, SAMPLE_SIZE_E_1_A_2);
    List<AbstractMeasurementCommand> measurements = Arrays.asList(measurementE1A1, measurementE1A2);
    EstimatesCommand command = new EstimatesCommand(measurements);
    Estimates result = statisticsService.getEstimates(command);

    assertNotNull(result);
    Estimate estimate = result.getEstimates().get(ENDPOINT_1_URI).get(0);
    assertEquals(1.0476190476190477, estimate.getPointEstimate(), 0.000001);
    assertEquals(0.564802625987712, estimate.getConfidenceIntervalLowerBound(), 0.000001);
    assertEquals(1.9431667248624263, estimate.getConfidenceIntervalUpperBound(), 0.000001);
    assertEquals(0.8797293910244681, estimate.getpValue(), 0.000001);
  }

  @Test
  public void testGetEstimatesForContinuous() {
    Double meanE1A1 = 10.;
    Double stdDevE1A1 = 2.2;
    Double meanE1A2 = 11.;
    Double stdDevE1A2 = 2.1;
    AbstractMeasurementCommand measurementE1A1 = new ContinuousMeasurementCommand(ENDPOINT_1_URI, ARM_1_URI, meanE1A1, stdDevE1A1, SAMPLE_SIZE_E_1_A_1);
    AbstractMeasurementCommand measurementE1A2 = new ContinuousMeasurementCommand(ENDPOINT_1_URI, ARM_2_URI, meanE1A2, stdDevE1A2, SAMPLE_SIZE_E_1_A_2);
    List<AbstractMeasurementCommand> measurements = Arrays.asList(measurementE1A1, measurementE1A2);
    EstimatesCommand command = new EstimatesCommand(measurements);
    Estimates result = statisticsService.getEstimates(command);

    assertNotNull(result);
    Estimate estimate = result.getEstimates().get(ENDPOINT_1_URI).get(0);
    assertEquals(1.0, estimate.getPointEstimate(), 0.000001);
    assertEquals(-0.3598742251457008, estimate.getConfidenceIntervalLowerBound(), 0.000001);
    assertEquals(2.3598742251457, estimate.getConfidenceIntervalUpperBound(), 0.000001);
    assertEquals(0.14494767628248684, estimate.getpValue(), 0.000001);
  }

}
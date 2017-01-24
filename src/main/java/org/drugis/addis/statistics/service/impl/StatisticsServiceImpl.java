package org.drugis.addis.statistics.service.impl;

import org.drugis.addis.statistics.command.AbstractMeasurementCommand;
import org.drugis.addis.statistics.command.ContinuousMeasurementCommand;
import org.drugis.addis.statistics.command.DichotomousMeasurementCommand;
import org.drugis.addis.statistics.command.EstimatesCommand;
import org.drugis.addis.statistics.model.*;
import org.drugis.addis.statistics.service.StatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by daan on 20-1-17.
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {
  private final static Logger logger = LoggerFactory.getLogger(StatisticsService.class);

  @Override
  public Estimates getEstimates(EstimatesCommand command) {
    List<AbstractMeasurementCommand> measurements = command.getMeasurements();
    if (measurements.isEmpty()){
      return null;
    }
    Map<URI, List<AbstractMeasurementCommand>> measurementsByEndpointAndArm = measurements.stream().collect(Collectors.groupingBy(AbstractMeasurementCommand::getEndpointUri));
    URI baselineUri = measurements.get(0).getArmUri();
    Map<URI, List<Estimate>> estimatesByEndpointUri = calculateEstimates(measurementsByEndpointAndArm, baselineUri);
    return new Estimates(baselineUri, estimatesByEndpointUri);
  }


  private Map<URI, List<Estimate>> calculateEstimates(Map<URI, List<AbstractMeasurementCommand>> measurementsByEndpointAndArm, URI baselineUri) {
    Collection<List<AbstractMeasurementCommand>> measurementsForEndpoints = measurementsByEndpointAndArm.values();
    Map<URI, List<Estimate>> result = new HashMap<>();
    measurementsForEndpoints.forEach(measurementsForEndpoint -> {
      AbstractMeasurementCommand baseline = null;
      List<AbstractMeasurementCommand> subjects = new ArrayList<>();
      for(AbstractMeasurementCommand measurement : measurementsForEndpoint) {
        if(measurement.getArmUri().equals(baselineUri)) {
          baseline = measurement;
        } else {
          subjects.add(measurement);
        }
      }
      List<Estimate> estimatesForEndpoint = getEstimatesForEndpoint(baseline, subjects);
      assert baseline != null;
      result.put(baseline.getEndpointUri(), estimatesForEndpoint);
    });
    return result;
  }

  private List<Estimate> getEstimatesForEndpoint(AbstractMeasurementCommand baseline, List<AbstractMeasurementCommand> subjects) {
    return subjects.stream().map(subject -> getEstimate(baseline, subject)).collect(Collectors.toList());
  }

  private Estimate getEstimate(AbstractMeasurementCommand baseline, AbstractMeasurementCommand subject) {
     // (baseline, subject) -> relEffect; releEffect -> distribution; distribution -> confidenceInterval; confidenceInterval -> quantiles(0.5, 0.05, 0.95), pvalue
    AbstractRelativeEffect relativeEffect = buildRelativeEffect(baseline, subject);
    Distribution distribution = relativeEffect.getDistribution();
    Double pointEstimate = distribution.getQuantile(0.5);
    Double confidenceIntervalLowerBound = distribution.getQuantile(0.025);
    Double confidenceIntervalUpperBound = distribution.getQuantile(0.975);
    double prob = distribution.getCumulativeProbability(relativeEffect.getNeutralValue());
    Double pValue = 2 * Math.min(prob, 1 - prob);
    return new Estimate(pointEstimate, confidenceIntervalLowerBound, confidenceIntervalUpperBound, pValue);
  }

  private AbstractRelativeEffect buildRelativeEffect(AbstractMeasurementCommand baseline, AbstractMeasurementCommand subject) {
    if(baseline instanceof DichotomousMeasurementCommand) {
      return new RiskRatio((DichotomousMeasurementCommand) baseline, (DichotomousMeasurementCommand) subject);
    } else if (baseline instanceof ContinuousMeasurementCommand) {
      return new MeanDifference((ContinuousMeasurementCommand) baseline, (ContinuousMeasurementCommand)subject);
    }
    return null;
  }

}

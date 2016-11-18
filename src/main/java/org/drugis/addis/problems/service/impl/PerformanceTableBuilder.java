package org.drugis.addis.problems.service.impl;

import org.apache.commons.lang3.tuple.Pair;
import org.drugis.addis.problems.service.ProblemService;
import org.drugis.addis.problems.service.model.*;
import org.drugis.addis.trialverse.model.trialdata.Measurement;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by daan on 3/27/14.
 */
@Service
public class PerformanceTableBuilder {

  public List<AbstractMeasurementEntry> build(Set<Pair<Measurement, Integer>> measurementDrugInstancePair) {
    ArrayList<AbstractMeasurementEntry> performanceTable = new ArrayList<>();
    for (Pair<Measurement, Integer> pair: measurementDrugInstancePair) {
      Measurement measurement = pair.getLeft();
      Integer interventionId = pair.getRight();
      if (measurement.getMeasurementTypeURI().equals(ProblemService.DICHOTOMOUS_TYPE_URI)) {
        performanceTable.add(createBetaDistributionEntry(interventionId, measurement.getVariableUri(),
                measurement.getRate(), measurement.getSampleSize()));
      } else if (measurement.getMeasurementTypeURI().equals(ProblemService.CONTINUOUS_TYPE_URI)) {
        performanceTable.add(createNormalDistributionEntry(interventionId, measurement.getVariableUri(),
                measurement.getMean(), measurement.getStdDev(), measurement.getSampleSize(), measurement.getStdErr()));
      } else {
        throw new IllegalArgumentException("Unknown measurement type: " + measurement.getMeasurementTypeURI());
      }
    }
    return performanceTable;
  }

  private ContinuousMeasurementEntry createNormalDistributionEntry(Integer interventionId, URI criterionUid, Double mean, Double standardDeviation, Integer sampleSize, Double standardError) {
    Double sigma = standardError != null ? standardError : standardDeviation / Math.sqrt(sampleSize);

    ContinuousPerformance performance = new ContinuousPerformance(new ContinuousPerformanceParameters(mean, sigma));
    return new ContinuousMeasurementEntry(interventionId, criterionUid, performance);
  }

  private RateMeasurementEntry createBetaDistributionEntry(Integer interventonId, URI criterionUri, Integer rate, Integer sampleSize) {
    int alpha = rate + 1;
    int beta = sampleSize - rate + 1;

    RatePerformance performance = new RatePerformance(new RatePerformanceParameters(alpha, beta));
    return new RateMeasurementEntry(interventonId, criterionUri, performance);
  }

}

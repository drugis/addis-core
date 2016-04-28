package org.drugis.addis.problems.service.impl;

import org.apache.commons.lang3.tuple.Pair;
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

  public List<AbstractMeasurementEntry> build(Set<Pair<Measurement, URI>> measurementDrugInstancePair) {
    ArrayList<AbstractMeasurementEntry> performanceTable = new ArrayList<>();
    for (Pair<Measurement, URI> pair: measurementDrugInstancePair) {
      Measurement measurement = pair.getLeft();
      URI drugInstanceUri = pair.getRight();
      if (measurement.getRate() != null) {

        performanceTable.add(createBetaDistributionEntry(drugInstanceUri, measurement.getVariableUri(), measurement.getRate(), measurement.getSampleSize()));
      } else if (measurement.getMean() != null) {
        performanceTable.add(createNormalDistributionEntry(drugInstanceUri, measurement.getVariableUri(), measurement.getMean(), measurement.getStdDev(), measurement.getSampleSize()));
      }
    }
    return performanceTable;
  }

  private ContinuousMeasurementEntry createNormalDistributionEntry(URI alternativeUri, URI criterionUid, Double mean, Double standardDeviation, Integer sampleSize) {
    Double sigma = standardDeviation / Math.sqrt(sampleSize);

    ContinuousPerformance performance = new ContinuousPerformance(new ContinuousPerformanceParameters(mean, sigma));
    return new ContinuousMeasurementEntry(alternativeUri, criterionUid, performance);
  }

  private RateMeasurementEntry createBetaDistributionEntry(URI alternativeUri, URI criterionUri, Integer rate, Integer sampleSize) {
    int alpha = rate + 1;
    int beta = sampleSize - rate + 1;

    RatePerformance performance = new RatePerformance(new RatePerformanceParameters(alpha, beta));
    return new RateMeasurementEntry(alternativeUri, criterionUri, performance);
  }

}

package org.drugis.addis.problems.service.impl;

import org.apache.commons.lang3.tuple.Triple;
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
public class SingleStudyPerformanceTableBuilder {

  public List<AbstractMeasurementEntry> build(Set<Triple<Measurement, Integer, String>> measurementDrugInstancePairs) {
    ArrayList<AbstractMeasurementEntry> performanceTable = new ArrayList<>();
    for (Triple<Measurement, Integer, String> pair: measurementDrugInstancePairs) {
      Measurement measurement = pair.getLeft();
      Integer interventionId = pair.getMiddle();
      String dataSource = pair.getRight();
      if (measurement.getMeasurementTypeURI().equals(ProblemService.DICHOTOMOUS_TYPE_URI)) {
        performanceTable.add(createBetaDistributionEntry(interventionId, measurement.getVariableConceptUri(), dataSource,
                measurement.getRate(), measurement.getSampleSize()));
      } else if (measurement.getMeasurementTypeURI().equals(ProblemService.CONTINUOUS_TYPE_URI)) {
        performanceTable.add(createNormalDistributionEntry(interventionId, measurement.getVariableConceptUri(), dataSource,
                measurement.getMean(), measurement.getStdDev(), measurement.getSampleSize(), measurement.getStdErr()));
      } else {
        throw new IllegalArgumentException("Unknown measurement type: " + measurement.getMeasurementTypeURI());
      }
    }
    return performanceTable;
  }

  private ContinuousMeasurementEntry createNormalDistributionEntry(Integer interventionId, URI criterionUri, String dataSourceUri, Double mean, Double standardDeviation, Integer sampleSize, Double standardError) {
    Double sigma = standardError != null ? standardError : standardDeviation / Math.sqrt(sampleSize);

    ContinuousPerformance performance = new ContinuousPerformance(new ContinuousPerformanceParameters(mean, sigma));
    return new ContinuousMeasurementEntry(interventionId, criterionUri.toString(), dataSourceUri, performance);
  }

  private RateMeasurementEntry createBetaDistributionEntry(Integer interventionId, URI criterionUri, String dataSourceUri, Integer rate, Integer sampleSize) {
    int alpha = rate + 1;
    int beta = sampleSize - rate + 1;

    RatePerformance performance = new RatePerformance(new RatePerformanceParameters(alpha, beta));
    return new RateMeasurementEntry(interventionId, criterionUri.toString(), dataSourceUri, performance);
  }

}

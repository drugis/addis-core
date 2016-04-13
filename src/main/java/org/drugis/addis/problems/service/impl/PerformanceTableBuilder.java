package org.drugis.addis.problems.service.impl;

import org.drugis.addis.problems.service.model.*;
import org.drugis.addis.trialverse.service.impl.TriplestoreServiceImpl;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by daan on 3/27/14.
 */
@Service
public class PerformanceTableBuilder {

  public List<AbstractMeasurementEntry> build(List<TriplestoreServiceImpl.SingleStudyBenefitRiskMeasurementRow> measurementNodes) {
    ArrayList<AbstractMeasurementEntry> performanceTable = new ArrayList<>();
    for (TriplestoreServiceImpl.SingleStudyBenefitRiskMeasurementRow measurementRow : measurementNodes) {
      if (measurementRow.getRate() != null) {
        performanceTable.add(createBetaDistributionEntry(measurementRow.getAlternativeUri(), measurementRow.getOutcomeUid(), measurementRow.getRate(), measurementRow.getSampleSize()));
      } else if (measurementRow.getMean() != null) {
        performanceTable.add(createNormalDistributionEntry(measurementRow.getAlternativeUri(), measurementRow.getOutcomeUid(), measurementRow.getMean(), measurementRow.getStdDev(), measurementRow.getSampleSize()));
      }
    }
    return performanceTable;
  }

  private ContinuousMeasurementEntry createNormalDistributionEntry(URI alternativeUri, String criterionUid, Double mean, Double standardDeviation, Integer sampleSize) {
    Double sigma = standardDeviation / Math.sqrt(sampleSize);

    ContinuousPerformance performance = new ContinuousPerformance(new ContinuousPerformanceParameters(mean, sigma));
    return new ContinuousMeasurementEntry(alternativeUri, criterionUid, performance);
  }

  private RateMeasurementEntry createBetaDistributionEntry(URI alternativeUri, String criterionUid, Integer rate, Integer sampleSize) {
    int alpha = rate + 1;
    int beta = sampleSize - rate + 1;

    RatePerformance performance = new RatePerformance(new RatePerformanceParameters(alpha, beta));
    return new RateMeasurementEntry(alternativeUri, criterionUid, performance);
  }

}

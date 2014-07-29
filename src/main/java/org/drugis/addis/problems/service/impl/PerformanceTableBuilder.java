package org.drugis.addis.problems.service.impl;

import org.drugis.addis.problems.service.model.*;
import org.drugis.addis.trialverse.service.impl.TriplestoreServiceImpl;
import org.springframework.stereotype.Service;

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
        performanceTable.add(createBetaDistributionEntry(measurementRow.getAlternativeUid(), measurementRow.getOutcomeUid(), measurementRow.getRate(), measurementRow.getSampleSize()));
      } else if (measurementRow.getMean() != null) {
        performanceTable.add(createNormalDistributionEntry(measurementRow.getAlternativeUid(), measurementRow.getOutcomeUid(), measurementRow.getMean(), measurementRow.getStdDev(), measurementRow.getSampleSize()));
      }
    }
    return performanceTable;
  }

  private ContinuousMeasurementEntry createNormalDistributionEntry(String alternativeUid, String criterionUid, Double mean, Double standardDeviation, Long sampleSize) {
    Double sigma = standardDeviation / Math.sqrt(sampleSize);

    ContinuousPerformance performance = new ContinuousPerformance(new ContinuousPerformanceParameters(mean, sigma));
    return new ContinuousMeasurementEntry(alternativeUid, criterionUid, performance);
  }

  private RateMeasurementEntry createBetaDistributionEntry(String alternativeUid, String criterionUid, Long rate, Long sampleSize) {
    Long alpha = rate + 1;
    Long beta = sampleSize - rate + 1;

    RatePerformance performance = new RatePerformance(new RatePerformanceParameters(alpha, beta));
    return new RateMeasurementEntry(alternativeUid, criterionUid, performance);
  }

}

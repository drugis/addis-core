package org.drugis.addis.problems.service.impl;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.drugis.addis.problems.model.AlternativeEntry;
import org.drugis.addis.problems.model.CriterionEntry;
import org.drugis.addis.problems.model.Measurement;
import org.drugis.addis.problems.service.model.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by daan on 3/27/14.
 */
@Service
public class PerformanceTableBuilder {

  public Map<Pair<AlternativeEntry, CriterionEntry>, Measurement> createPerformanceMap(
          Map<String, CriterionEntry> criteria, Map<String, AlternativeEntry> alternatives, List<Measurement> measurements) {
    Map<Pair<AlternativeEntry, CriterionEntry>, Measurement> performanceMap = new HashMap<>();

    for (Measurement measurement : measurements) {
      AlternativeEntry alternativeEntry = alternatives.get(measurement.getArmUid());
      CriterionEntry criterionEntry = criteria.get(measurement.getVariableUid());
      Pair<AlternativeEntry, CriterionEntry> key = new ImmutablePair<>(alternativeEntry, criterionEntry);
      performanceMap.put(key, measurement);
    }
    return performanceMap;
  }

  public List<AbstractMeasurementEntry> build(Map<String, CriterionEntry> criteria, Map<String, AlternativeEntry> alternatives, List<Measurement> measurements) {
    Map<Pair<AlternativeEntry, CriterionEntry>, Measurement> measurementsMap = createPerformanceMap(criteria, alternatives, measurements);
    ArrayList<AbstractMeasurementEntry> performanceTable = new ArrayList<>();

    for (Map.Entry<Pair<AlternativeEntry, CriterionEntry>, Measurement> entry : measurementsMap.entrySet()) {
      AlternativeEntry alternativeEntry = entry.getKey().getLeft();
      CriterionEntry criterionEntry = entry.getKey().getRight();
      Measurement measurement = entry.getValue();
      if (measurement.getRate() != null) {
        performanceTable.add(createBetaDistributionEntry(alternativeEntry, criterionEntry, measurement));
      } else if (measurement.getMean() != null) {
        performanceTable.add(createNormalDistributionEntry(alternativeEntry, criterionEntry, measurement));
      }
    }

    return performanceTable;
  }

  public ContinuousMeasurementEntry createNormalDistributionEntry(AlternativeEntry alternativeEntry, CriterionEntry criterionEntry, Measurement measurement) {
    Double mean = measurement.getMean();
    Double standardDeviation = measurement.getStdDev();
    Long sampleSize = measurement.getSampleSize();
    Double sigma = standardDeviation / Math.sqrt(sampleSize);

    ContinuousPerformance performance = new ContinuousPerformance(new ContinuousPerformanceParameters(mean, sigma));
    return new ContinuousMeasurementEntry(alternativeEntry.getAlternativeUri(), criterionEntry.getCriterionUri(), performance);
  }

  public RateMeasurementEntry createBetaDistributionEntry(AlternativeEntry alternativeEntry, CriterionEntry criterionEntry, Measurement measurement) {
    Long rate = measurement.getRate();
    Long sampleSize = measurement.getSampleSize();

    Long alpha = rate + 1;
    Long beta = sampleSize - rate + 1;

    RatePerformance performance = new RatePerformance(new RatePerformanceParameters(alpha, beta));
    return new RateMeasurementEntry(alternativeEntry.getAlternativeUri(), criterionEntry.getCriterionUri(), performance);
  }

}

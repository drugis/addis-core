package org.drugis.addis.problems.service.impl;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.drugis.addis.problems.model.AlternativeEntry;
import org.drugis.addis.problems.model.CriterionEntry;
import org.drugis.addis.problems.model.Measurement;
import org.drugis.addis.problems.model.MeasurementAttribute;
import org.drugis.addis.problems.service.model.*;
import org.drugis.addis.util.JSONUtils;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by daan on 3/27/14.
 */
@Service
public class PerformanceTableBuilder {

  @Inject
  JSONUtils jsonUtils;

  public Map<Pair<AlternativeEntry, CriterionEntry>, Map<MeasurementAttribute, Measurement>> createPerformanceMap(
          Map<Long, CriterionEntry> criteria, Map<Long, AlternativeEntry> alternatives, List<Measurement> measurements) {
    Map<Pair<AlternativeEntry, CriterionEntry>, Map<MeasurementAttribute, Measurement>> performanceMap = new HashMap<>();

    for (Measurement measurement : measurements) {
      AlternativeEntry alternativeEntry = alternatives.get(measurement.getArmId());
      CriterionEntry criterionEntry= criteria.get(measurement.getVariableId());
      Pair<AlternativeEntry, CriterionEntry> key = new ImmutablePair<>(alternativeEntry, criterionEntry);
      if (!performanceMap.containsKey(key)) {
        performanceMap.put(key, new HashMap<MeasurementAttribute, Measurement>());
      }
      performanceMap.get(key).put(measurement.getMeasurementAttribute(), measurement);
    }
    System.out.println("created performance map " + performanceMap);
    return performanceMap;
  }

  public List<AbstractMeasurementEntry> build(Map<Long, CriterionEntry> criteria, Map<Long, AlternativeEntry> alternatives, List<Measurement> measurements) {
    Map<Pair<AlternativeEntry, CriterionEntry>, Map<MeasurementAttribute, Measurement>> measurementsMap = createPerformanceMap(criteria, alternatives, measurements);
    ArrayList<AbstractMeasurementEntry> performanceTable = new ArrayList<>();

    for (Map.Entry<Pair<AlternativeEntry, CriterionEntry>, Map<MeasurementAttribute, Measurement>> entry : measurementsMap.entrySet()) {
      AlternativeEntry alternativeEntry = entry.getKey().getLeft();
      CriterionEntry criterionEntry = entry.getKey().getRight();
      Map<MeasurementAttribute, Measurement> measurementMap = entry.getValue();
      if (measurementMap.get(MeasurementAttribute.RATE) != null) {
        performanceTable.add(createBetaDistributionEntry(alternativeEntry, criterionEntry, measurementMap));
      } else if (measurementMap.get(MeasurementAttribute.MEAN) != null) {
        performanceTable.add(createNormalDistributionEntry(alternativeEntry, criterionEntry, measurementMap));
      }
    }

    return performanceTable;
  }

  public ContinuousMeasurementEntry createNormalDistributionEntry(AlternativeEntry alternativeEntry, CriterionEntry criterionEntry, Map<MeasurementAttribute, Measurement> measurementMap) {
    assert (measurementMap.size() == 3);
    Measurement mean = measurementMap.get(MeasurementAttribute.MEAN);
    Measurement standardDeviation = measurementMap.get(MeasurementAttribute.STANDARD_DEVIATION);
    Measurement sampleSize = measurementMap.get(MeasurementAttribute.SAMPLE_SIZE);

    String alternativeName = alternativeEntry.getTitle();
    String criterionName = criterionEntry.getTitle();

    Double mu = mean.getRealValue();
    Double sigma = standardDeviation.getRealValue() / Math.sqrt(sampleSize.getIntegerValue());

    ContinuousPerformance performance = new ContinuousPerformance(new ContinuousPerformanceParameters(mu, sigma));
    return new ContinuousMeasurementEntry(jsonUtils.createKey(alternativeName), jsonUtils.createKey(criterionName), performance);
  }

  public RateMeasurementEntry createBetaDistributionEntry(AlternativeEntry alternativeEntry, CriterionEntry criterionEntry, Map<MeasurementAttribute, Measurement> measurementMap) {
    assert (measurementMap.size() == 2);
    Measurement rate = measurementMap.get(MeasurementAttribute.RATE);
    Measurement sampleSize = measurementMap.get(MeasurementAttribute.SAMPLE_SIZE);

    Long alpha = rate.getIntegerValue() + 1;
    Long beta = sampleSize.getIntegerValue() - rate.getIntegerValue() + 1;

    String alternativeName = alternativeEntry.getTitle();
    String criterionName = criterionEntry.getTitle();
    RatePerformance performance = new RatePerformance(new RatePerformanceParameters(alpha, beta));
    return new RateMeasurementEntry(jsonUtils.createKey(alternativeName), jsonUtils.createKey(criterionName), performance);
  }

}

package org.drugis.addis.problems.service.impl;

import org.drugis.addis.problems.model.Arm;
import org.drugis.addis.problems.model.Measurement;
import org.drugis.addis.problems.model.MeasurementAttribute;
import org.drugis.addis.problems.model.Variable;
import org.drugis.addis.problems.service.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by daan on 3/27/14.
 */
public class PerformanceTableBuilder {

  Map<Long, Variable> variableMap = new HashMap<>();
  Map<Long, Arm> armMap = new HashMap<>();
  List<Measurement> measurements;


  public PerformanceTableBuilder(List<Variable> variables, List<Arm> arms, List<Measurement> measurements) {
    for (Variable variable : variables) {
      variableMap.put(new Long(variable.getId()), variable);
    }

    for (Arm arm : arms) {
      armMap.put(arm.getId(), arm);
    }

    this.measurements = measurements;
  }

  public Map<Map<Arm, Variable>, Map<MeasurementAttribute, Measurement>> createPerformanceMap() {
    Map<Map<Arm, Variable>, Map<MeasurementAttribute, Measurement>> performanceMap = new HashMap<>();

    for (Measurement measurement : measurements) {
      Map<Arm, Variable> key = new HashMap<>();
      Arm arm = armMap.get(measurement.getArmId());
      Variable variable = variableMap.get(measurement.getVariableId());
      key.put(arm, variable);
      if (!performanceMap.containsKey(key)) {
        performanceMap.put(key, new HashMap<MeasurementAttribute, Measurement>());
      }
      performanceMap.get(key).put(measurement.getMeasurementAttribute(), measurement);
    }
    return performanceMap;
  }

  public List<AbstractMeasurementEntry> build() {
    Map<Map<Arm, Variable>, Map<MeasurementAttribute, Measurement>> measurementsMap = createPerformanceMap();

    ArrayList<AbstractMeasurementEntry> performanceTable = new ArrayList<>();
    for (Map<MeasurementAttribute, Measurement> measurementMap : measurementsMap.values()) {
      if (measurementMap.get(MeasurementAttribute.RATE) != null) {
        performanceTable.add(createBetaDistributionEntry(measurementMap));
      } else if (measurementMap.get(MeasurementAttribute.MEAN) != null) {
        performanceTable.add(createNormalDistributionEntry(measurementMap));
      }
    }
    return performanceTable;
  }

  public ContinuousMeasurementEntry createNormalDistributionEntry(Map<MeasurementAttribute, Measurement> measurementMap) {
    assert (measurementMap.size() == 3);
    Measurement mean = measurementMap.get(MeasurementAttribute.MEAN);
    Measurement standardDeviation = measurementMap.get(MeasurementAttribute.STANDARD_DEVIATION);

    String armName = armMap.get(mean.getArmId()).getName();
    String variableName = variableMap.get(mean.getVariableId()).getName();

    Double mu = mean.getRealValue();
    Double sigma = standardDeviation.getRealValue();

    ContinuousPerformance performance = new ContinuousPerformance(new ContinuousPerformanceParameters(mu, sigma));
    return new ContinuousMeasurementEntry(armName, variableName, performance);
  }

  public RateMeasurementEntry createBetaDistributionEntry(Map<MeasurementAttribute, Measurement> measurementMap) {
    assert (measurementMap.size() == 2);
    Measurement rate = measurementMap.get(MeasurementAttribute.RATE);
    Measurement sampleSize = measurementMap.get(MeasurementAttribute.SAMPLE_SIZE);

    Long alpha = rate.getIntegerValue() + 1;
    Long beta = sampleSize.getIntegerValue() - rate.getIntegerValue() + 1;

    String armName = armMap.get(rate.getArmId()).getName();
    String variableName = variableMap.get(rate.getVariableId()).getName();
    RatePerformance performance = new RatePerformance(new RatePerformanceParameters(alpha, beta));
    return new RateMeasurementEntry(armName, variableName, performance);
  }

}

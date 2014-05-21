package org.drugis.addis.problems.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by daan on 3/26/14.
 */
public class Measurement {

  Long studyId;
  Long variableId;
  Long measurementMomentId;
  Long armId;
  MeasurementAttribute measurementAttribute;
  Long integerValue;
  Double realValue;

  public Measurement() {
  }

  public Measurement(Long studyId, Long variableId, Long measurementMomentId, Long armId, MeasurementAttribute measurementAttribute, Long integerValue, Double realValue) {
    this.studyId = studyId;
    this.variableId = variableId;
    this.measurementMomentId = measurementMomentId;
    this.armId = armId;
    this.measurementAttribute = measurementAttribute;
    this.integerValue = integerValue;
    this.realValue = realValue;
  }

  public Long getStudyId() {
    return studyId;
  }

  public Long getVariableId() {
    return variableId;
  }

  public Long getMeasurementMomentId() {
    return measurementMomentId;
  }

  public Long getArmId() {
    return armId;
  }

  public MeasurementAttribute getMeasurementAttribute() {
    return measurementAttribute;
  }

  public Long getIntegerValue() {
    return integerValue;
  }

  public Double getRealValue() {
    return realValue;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Measurement that = (Measurement) o;

    if (!armId.equals(that.armId)) return false;
    if (integerValue != null ? !integerValue.equals(that.integerValue) : that.integerValue != null) return false;
    if (measurementAttribute != that.measurementAttribute) return false;
    if (!measurementMomentId.equals(that.measurementMomentId)) return false;
    if (realValue != null ? !realValue.equals(that.realValue) : that.realValue != null) return false;
    if (!studyId.equals(that.studyId)) return false;
    if (!variableId.equals(that.variableId)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = studyId.hashCode();
    result = 31 * result + variableId.hashCode();
    result = 31 * result + measurementMomentId.hashCode();
    result = 31 * result + armId.hashCode();
    result = 31 * result + measurementAttribute.hashCode();
    result = 31 * result + (integerValue != null ? integerValue.hashCode() : 0);
    result = 31 * result + (realValue != null ? realValue.hashCode() : 0);
    return result;
  }

  public static Map<MeasurementAttribute, Measurement> mapMeasurementsByAttribute(List<Measurement> measurements) {
    Map<MeasurementAttribute, Measurement> measurementsByAttribute = new HashMap<>();
    for(Measurement measurement : measurements) {
      measurementsByAttribute.put(measurement.getMeasurementAttribute(), measurement);
    }
    return measurementsByAttribute;
  }
}

package org.drugis.addis.trialverse.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
@Entity
@Table(name = "measurements")
public class Measurement {
  @EmbeddedId
  @JsonIgnore
  private MeasurementKey measurementKey;

  @Column(name = "integer_value")
  Long integerValue;
  @Column(name = "real_value")
  Double realValue;

  public Measurement() {
  }

  public Measurement(Long studyId, Long variableId, Long measurementMomentId, Long armId, MeasurementAttribute measurementAttribute, Long integerValue, Double realValue) {
    this.measurementKey = new MeasurementKey(studyId,variableId, measurementMomentId, armId, measurementAttribute);
    this.integerValue = integerValue;
    this.realValue = realValue;
  }

  public MeasurementKey getMeasurementKey() {
    return measurementKey;
  }

  public Long getIntegerValue() {
    return integerValue;
  }

  public Double getRealValue() {
    return realValue;
  }

  public Long getStudyId() { return measurementKey.getStudyId(); }

  public Long getVariableId() { return measurementKey.getVariableId(); }

  public Long getArmId() { return measurementKey.getArmId(); }

  public Long getMeasurementMomentId() {return measurementKey.getMeasurementMomentId(); }

  public MeasurementAttribute getMeasurementAttribute() { return measurementKey.getMeasurementAttribute(); }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Measurement that = (Measurement) o;

    if (!integerValue.equals(that.integerValue)) return false;
    if (!measurementKey.equals(that.measurementKey)) return false;
    if (!realValue.equals(that.realValue)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = measurementKey.hashCode();
    result = 31 * result + integerValue.hashCode();
    result = 31 * result + realValue.hashCode();
    return result;
  }
}

package org.drugis.addis.trialverse.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

  @Column(name = "attribute")
  @Type(type = "org.drugis.addis.trialverse.model.MeasurementAttributeUserType")
  MeasurementAttribute measurementAttribute;
  @Column(name = "integer_value")
  Long integerValue;
  @Column(name = "real_value")
  Double realValue;

  public Measurement() {
  }

  public Measurement(Long studyId, Long variableId, Long measurementMomentId, Long armId, MeasurementAttribute measurementAttribute, Long integerValue, Double realValue) {
    this.measurementKey = new MeasurementKey(studyId,variableId, measurementMomentId, armId);
    this.measurementAttribute = measurementAttribute;
    this.integerValue = integerValue;
    this.realValue = realValue;
  }

  public MeasurementKey getMeasurementKey() {
    return measurementKey;
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

  public Long getStudyId() { return measurementKey.getStudyId(); }

  public Long getVariableId() { return measurementKey.getVariableId(); }

  public Long getArmId() { return measurementKey.getArmId(); }

  public Long getMeasurementMomentId() {return measurementKey.getMeasurementMomentId(); }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Measurement that = (Measurement) o;

    if (integerValue != null ? !integerValue.equals(that.integerValue) : that.integerValue != null) return false;
    if (measurementAttribute != that.measurementAttribute) return false;
    if (!measurementKey.equals(that.measurementKey)) return false;
    if (realValue != null ? !realValue.equals(that.realValue) : that.realValue != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = measurementKey.hashCode();
    result = 31 * result + measurementAttribute.hashCode();
    result = 31 * result + (integerValue != null ? integerValue.hashCode() : 0);
    result = 31 * result + (realValue != null ? realValue.hashCode() : 0);
    return result;
  }
}

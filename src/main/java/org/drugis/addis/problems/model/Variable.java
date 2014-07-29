package org.drugis.addis.problems.model;

/**
 * Created by connor on 25-3-14.
 */
public class Variable {

  private String uid;
  private String study;
  private String name;
  private String description;
  private String unitDescription;
  private Boolean isPrimary;
  private MeasurementType measurementType;
  private String variableType;

  public Variable() {
  }

  public Variable(String Uid, String studyUid, String name, String description, String unitDescription, Boolean isPrimary, MeasurementType measurementType, String variableType) {
    this.uid = Uid;
    this.study = studyUid;
    this.name = name;
    this.description = description;
    this.unitDescription = unitDescription;
    this.isPrimary = isPrimary;
    this.measurementType = measurementType;
    this.variableType = variableType;
  }

  public String getUid() {
    return uid;
  }

  public String getStudy() {
    return study;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getUnitDescription() {
    return unitDescription;
  }

  public Boolean getIsPrimary() {
    return isPrimary;
  }

  public MeasurementType getMeasurementType() {
    return measurementType;
  }

  public String getVariableType() {
    return variableType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Variable variable = (Variable) o;

    if (description != null ? !description.equals(variable.description) : variable.description != null) return false;
    if (!uid.equals(variable.uid)) return false;
    if (!isPrimary.equals(variable.isPrimary)) return false;
    if (measurementType != variable.measurementType) return false;
    if (!name.equals(variable.name)) return false;
    if (!study.equals(variable.study)) return false;
    if (unitDescription != null ? !unitDescription.equals(variable.unitDescription) : variable.unitDescription != null)
      return false;
    if (!variableType.equals(variable.variableType)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = uid.hashCode();
    result = 31 * result + study.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + (description != null ? description.hashCode() : 0);
    result = 31 * result + (unitDescription != null ? unitDescription.hashCode() : 0);
    result = 31 * result + isPrimary.hashCode();
    result = 31 * result + measurementType.hashCode();
    result = 31 * result + variableType.hashCode();
    return result;
  }
}

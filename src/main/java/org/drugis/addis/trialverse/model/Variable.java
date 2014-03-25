package org.drugis.addis.trialverse.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by connor on 25-3-14.
 */
@Entity
@Table(name = "variables")
public class Variable {

  @Id
  private Long id;
  private Long study;
  private String name;
  private String description;

  @Column(name = "unit_description")
  private String unitDescription;

  @Column(name = "is_primary")
  private Boolean isPrimary;

  @Column(name = "measurement_type")
  private String measurementType;

  @Column(name = "variable_type")
  private String variableType;

  public Variable() {
  }

  public Variable(Long id, Long study, String name, String description, String unitDescription, Boolean isPrimary, String measurementType, String variableType) {
    this.id = id;
    this.study = study;
    this.name = name;
    this.description = description;
    this.unitDescription = unitDescription;
    this.isPrimary = isPrimary;
    this.measurementType = measurementType;
    this.variableType = variableType;
  }

  public Long getId() {
    return id;
  }

  public Long getStudy() {
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

  public String getMeasurementType() {
    return measurementType;
  }

  public String getVariableType() {
    return variableType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Variable)) return false;

    Variable variable = (Variable) o;

    if (description != null ? !description.equals(variable.description) : variable.description != null) return false;
    if (!id.equals(variable.id)) return false;
    if (!isPrimary.equals(variable.isPrimary)) return false;
    if (!measurementType.equals(variable.measurementType)) return false;
    if (!name.equals(variable.name)) return false;
    if (!study.equals(variable.study)) return false;
    if (unitDescription != null ? !unitDescription.equals(variable.unitDescription) : variable.unitDescription != null)
      return false;
    if (!variableType.equals(variable.variableType)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
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

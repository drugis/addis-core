package org.drugis.addis.trialverse.model;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

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
  @Type(type = "org.drugis.addis.trialverse.repository.PGEnumUserType",
    parameters = {@Parameter(name = "enumClassName", value = "org.drugis.addis.trialverse.model.MeasurementType")})
  private MeasurementType measurementType;

  @Column(name = "variable_type")
  @Type(type = "org.drugis.addis.trialverse.repository.PGEnumUserType",
    parameters = {@Parameter(name = "enumClassName", value = "org.drugis.addis.trialverse.model.VariableType")})
  private VariableType variableType;

  public Variable() {
  }

  public Variable(Long id, Long study, String name, String description, String unitDescription,
                  Boolean isPrimary, MeasurementType measurementType, VariableType variableType) {
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

  public MeasurementType getMeasurementType() {
    return measurementType;
  }

  public VariableType getVariableType() {
    return variableType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Variable variable = (Variable) o;

    if (description != null ? !description.equals(variable.description) : variable.description != null) return false;
    if (!id.equals(variable.id)) return false;
    if (!isPrimary.equals(variable.isPrimary)) return false;
    if (measurementType != variable.measurementType) return false;
    if (!name.equals(variable.name)) return false;
    if (!study.equals(variable.study)) return false;
    if (unitDescription != null ? !unitDescription.equals(variable.unitDescription) : variable.unitDescription != null)
      return false;
    if (variableType != variable.variableType) return false;

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

package org.drugis.addis.covariates;

import org.drugis.addis.trialverse.model.emun.CovariateOptionType;

import javax.persistence.*;

/**
 * Created by connor on 12/1/15.
 */
@Entity
public class Covariate {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;
  private Integer project;
  private String name;
  private String motivation;

  private String definitionKey;
  @Enumerated(EnumType.STRING)
  private CovariateOptionType type;

  public Covariate() {
  }

  public Covariate(Integer id, Integer project, String name, String motivation, String definitionKey, CovariateOptionType type) {
    this.id = id;
    this.project = project;
    this.name = name;
    this.motivation = motivation;
    this.definitionKey = definitionKey;
    this.type = type;
  }

  public Covariate(Integer project, String name, String motivation, String definitionKey, CovariateOptionType type) {
    this(null, project, name, motivation, definitionKey, type);
  }

  public Integer getId() {
    return id;
  }

  public Integer getProject() {
    return project;
  }

  public String getName() {
    return name;
  }

  public String getMotivation() {
    return motivation;
  }

  public String getDefinitionKey() {
    return definitionKey;
  }

  public CovariateOptionType getType() {
    return type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Covariate covariate = (Covariate) o;

    if (id != null ? !id.equals(covariate.id) : covariate.id != null) return false;
    if (!project.equals(covariate.project)) return false;
    if (!name.equals(covariate.name)) return false;
    if (motivation != null ? !motivation.equals(covariate.motivation) : covariate.motivation != null) return false;
    if (!definitionKey.equals(covariate.definitionKey)) return false;
    return type == covariate.type;

  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + project.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + (motivation != null ? motivation.hashCode() : 0);
    result = 31 * result + definitionKey.hashCode();
    result = 31 * result + type.hashCode();
    return result;
  }
}

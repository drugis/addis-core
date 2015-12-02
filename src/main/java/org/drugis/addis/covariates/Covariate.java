package org.drugis.addis.covariates;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

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

  public Covariate() {
  }

  public Covariate(Integer project, String name, String motivation, String definitionKey) {
    this.project = project;
    this.name = name;
    this.motivation = motivation;
    CovariateOption.fromKey(definitionKey); // check if key is a valid definition
    this.definitionKey = definitionKey;
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

  public CovariateOption getDefinition() {
    return CovariateOption.fromKey(this.definitionKey);
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
    return definitionKey.equals(covariate.definitionKey);

  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + project.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + (motivation != null ? motivation.hashCode() : 0);
    result = 31 * result + definitionKey.hashCode();
    return result;
  }
}

package org.drugis.addis.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Created by daan on 22-5-14.
 */
@Entity
public class Model {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private Integer analysisId;

  public Model() {
  }

  public Model(Integer id, Integer analysisId) {
    this.id = id;
    this.analysisId = analysisId;
  }

  public Model(Integer analysisId) {
    this.analysisId = analysisId;
  }

  public Integer getId() {
    return id;
  }

  public Integer getAnalysisId() {
    return analysisId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Model)) return false;

    Model model = (Model) o;

    if (!analysisId.equals(model.analysisId)) return false;
    if (id != null ? !id.equals(model.id) : model.id != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + analysisId.hashCode();
    return result;
  }
}

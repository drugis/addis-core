package org.drugis.addis.models;

import com.fasterxml.jackson.annotation.JsonRawValue;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by joris on 2-3-17.
 */
@Entity
public class ModelBaseline {
  @Id
  private Integer modelId;
  private String baseline;

  public ModelBaseline() {
  }

  public ModelBaseline(Integer modelId, String baseline) {
    this.modelId = modelId;
    this.baseline = baseline;
  }

  public Integer getModelId() {
    return modelId;
  }

  @JsonRawValue
  public String getBaseline() {
    return baseline;
  }

  public void setBaseline(String baseline) {
    this.baseline = baseline;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ModelBaseline that = (ModelBaseline) o;

    if (!modelId.equals(that.modelId)) return false;
    return baseline != null ? baseline.equals(that.baseline) : that.baseline == null;
  }

  @Override
  public int hashCode() {
    int result = modelId.hashCode();
    result = 31 * result + (baseline != null ? baseline.hashCode() : 0);
    return result;
  }
}

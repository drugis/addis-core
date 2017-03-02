package org.drugis.addis.problems.model.modelBaseline;

import org.drugis.addis.problems.model.Baseline;

import javax.persistence.Entity;

/**
 * Created by joris on 2-3-17.
 */
@Entity
public class ModelBaseline {
  private Integer modelId;
  private Baseline baseline;

  public ModelBaseline() {
  }

  public ModelBaseline(Integer modelId, Baseline baseline) {
    this.modelId = modelId;
    this.baseline = baseline;
  }

  public Integer getModelId() {
    return modelId;
  }

  public void setModelId(Integer modelId) {
    this.modelId = modelId;
  }

  public Baseline getBaseline() {
    return baseline;
  }

  public void setBaseline(Baseline baseline) {
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

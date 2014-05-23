package org.drugis.addis.models;

/**
 * Created by daan on 22-5-14.
 */
public class Model {
  private Integer modelId;

  public Model() {
  }

  public Model(Integer modelId) {
    this.modelId = modelId;
  }

  public Integer getModelId() {
    return modelId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Model)) return false;

    Model model = (Model) o;

    if (!modelId.equals(model.modelId)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return modelId.hashCode();
  }
}

package org.drugis.addis.models;

/**
 * Created by connor on 6/24/15.
 */
public class ModelCommand {

  String title;
  String linearModel;
  ModelTypeCommand modelType;
  Integer burnInIterations;
  Integer inferenceIterations;
  Integer thinningFactor;

  public ModelCommand() {
  }

  public ModelCommand(String title, String linearModel, ModelTypeCommand modelType, Integer burnInIterations, Integer inferenceIterations, Integer thinningFactor) {
    this.title = title;
    this.linearModel = linearModel;
    this.modelType = modelType;
    this.burnInIterations = burnInIterations;
    this.inferenceIterations = inferenceIterations;
    this.thinningFactor = thinningFactor;
  }

  public Integer getBurnInIterations() {
    return burnInIterations;
  }

  public Integer getInferenceIterations() {
    return inferenceIterations;
  }

  public Integer getThinningFactor() {
    return thinningFactor;
  }

  public String getTitle() {
    return title;
  }

  public String getLinearModel() {
    return linearModel;
  }

  public ModelTypeCommand getModelType() {
    return modelType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ModelCommand that = (ModelCommand) o;

    if (!title.equals(that.title)) return false;
    if (!linearModel.equals(that.linearModel)) return false;
    if (!modelType.equals(that.modelType)) return false;
    if (!burnInIterations.equals(that.burnInIterations)) return false;
    if (!inferenceIterations.equals(that.inferenceIterations)) return false;
    return thinningFactor.equals(that.thinningFactor);

  }

  @Override
  public int hashCode() {
    int result = title.hashCode();
    result = 31 * result + linearModel.hashCode();
    result = 31 * result + modelType.hashCode();
    result = 31 * result + burnInIterations.hashCode();
    result = 31 * result + inferenceIterations.hashCode();
    result = 31 * result + thinningFactor.hashCode();
    return result;
  }
}

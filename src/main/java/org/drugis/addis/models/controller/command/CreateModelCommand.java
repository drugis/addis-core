package org.drugis.addis.models.controller.command;

/**
 * Created by connor on 6/24/15.
 */
public class CreateModelCommand {

  private String title;
  private String linearModel;
  private ModelTypeCommand modelType;
  private HeterogeneityPriorCommand heterogeneityPrior;
  private Integer burnInIterations;
  private Integer inferenceIterations;
  private Integer thinningFactor;
  private String likelihood;
  private String link;

  private Double outcomeScale;

  public CreateModelCommand() {
  }

  public CreateModelCommand(String title, String linearModel, ModelTypeCommand modelType, HeterogeneityPriorCommand heterogeneityPriorCommand, Integer burnInIterations, Integer inferenceIterations, Integer thinningFactor, String likelihood, String link) {
    this(title, linearModel, modelType, heterogeneityPriorCommand, burnInIterations, inferenceIterations, thinningFactor, likelihood, link, null);
  }

  public CreateModelCommand(String title, String linearModel, ModelTypeCommand modelType, Integer burnInIterations, Integer inferenceIterations, Integer thinningFactor, String likelihood, String link) {
    this(title, linearModel, modelType, null, burnInIterations, inferenceIterations, thinningFactor, likelihood, link);
  }

  public CreateModelCommand(String title, String linearModel, ModelTypeCommand modelType, Integer burnInIterations, Integer inferenceIterations, Integer thinningFactor, String likelihood, String link, Double outcomeScale) {
    this(title, linearModel, modelType, null, burnInIterations, inferenceIterations, thinningFactor, likelihood, link, outcomeScale);
  }

  public CreateModelCommand(String title, String linearModel, ModelTypeCommand modelType, HeterogeneityPriorCommand heterogeneityPriorCommand, Integer burnInIterations, Integer inferenceIterations, Integer thinningFactor, String likelihood, String link, Double outcomeScale) {
    this.title = title;
    this.linearModel = linearModel;
    this.modelType = modelType;
    this.heterogeneityPrior = heterogeneityPriorCommand;
    this.burnInIterations = burnInIterations;
    this.inferenceIterations = inferenceIterations;
    this.thinningFactor = thinningFactor;
    this.likelihood = likelihood;
    this.link = link;
    this.outcomeScale = outcomeScale;
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

  public HeterogeneityPriorCommand getHeterogeneityPrior() {
    return heterogeneityPrior;
  }

  public String getLikelihood() {
    return likelihood;
  }

  public String getLink() {
    return link;
  }

  public Double getOutcomeScale() {
    return outcomeScale;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CreateModelCommand that = (CreateModelCommand) o;

    if (!title.equals(that.title)) return false;
    if (!linearModel.equals(that.linearModel)) return false;
    if (!modelType.equals(that.modelType)) return false;
    if (heterogeneityPrior != null ? !heterogeneityPrior.equals(that.heterogeneityPrior) : that.heterogeneityPrior != null)
      return false;
    if (!burnInIterations.equals(that.burnInIterations)) return false;
    if (!inferenceIterations.equals(that.inferenceIterations)) return false;
    if (!thinningFactor.equals(that.thinningFactor)) return false;
    if (!likelihood.equals(that.likelihood)) return false;
    if (!link.equals(that.link)) return false;
    return !(outcomeScale != null ? !outcomeScale.equals(that.outcomeScale) : that.outcomeScale != null);

  }

  @Override
  public int hashCode() {
    int result = title.hashCode();
    result = 31 * result + linearModel.hashCode();
    result = 31 * result + modelType.hashCode();
    result = 31 * result + (heterogeneityPrior != null ? heterogeneityPrior.hashCode() : 0);
    result = 31 * result + burnInIterations.hashCode();
    result = 31 * result + inferenceIterations.hashCode();
    result = 31 * result + thinningFactor.hashCode();
    result = 31 * result + likelihood.hashCode();
    result = 31 * result + link.hashCode();
    result = 31 * result + (outcomeScale != null ? outcomeScale.hashCode() : 0);
    return result;
  }
}

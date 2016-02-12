package org.drugis.addis.models.controller.command;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import net.minidev.json.JSONObject;

/**
 * Created by connor on 6/24/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateModelCommand {

  private Integer burnInIterations;
  private HeterogeneityPriorCommand heterogeneityPrior;
  private Integer inferenceIterations;
  private String likelihood;
  private String linearModel;
  private String link;
  private ModelTypeCommand modelType;
  private Double outcomeScale;
  private JSONObject regressor;
  private JSONObject sensitivity;
  private Integer thinningFactor;
  private String title;

  public CreateModelCommand() {
  }

  protected CreateModelCommand(CreateModelCommandBuilder builder) {
    this.title = builder.title;
    this.linearModel = builder.linearModel;
    this.modelType = builder.modelType;
    this.heterogeneityPrior = builder.heterogeneityPriorCommand;
    this.burnInIterations = builder.burnInIterations;
    this.inferenceIterations = builder.inferenceIterations;
    this.thinningFactor = builder.thinningFactor;
    this.likelihood = builder.likelihood;
    this.link = builder.link;
    this.outcomeScale = builder.outcomeScale;
    this.regressor = builder.regressor;
    this.sensitivity = builder.sensitivity;
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

  public JSONObject getRegressor() {
    return regressor;
  }

  public JSONObject getSensitivity() {
    return sensitivity;
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

  public static class CreateModelCommandBuilder {

    private String title;
    private String linearModel;
    private ModelTypeCommand modelType;
    private HeterogeneityPriorCommand heterogeneityPriorCommand = null;
    private Integer burnInIterations;
    private Integer inferenceIterations;
    private Integer thinningFactor;
    private String likelihood;
    private String link;
    private Double outcomeScale = null;
    private JSONObject regressor;
    private JSONObject sensitivity;

    public CreateModelCommandBuilder setTitle(String title) {
      this.title = title;
      return this;
    }

    public CreateModelCommandBuilder setLinearModel(String linearModel) {
      this.linearModel = linearModel;
      return this;
    }

    public CreateModelCommandBuilder setModelType(ModelTypeCommand modelType) {
      this.modelType = modelType;
      return this;
    }

    public CreateModelCommandBuilder setHeterogeneityPriorCommand(HeterogeneityPriorCommand heterogeneityPriorCommand) {
      this.heterogeneityPriorCommand = heterogeneityPriorCommand;
      return this;
    }

    public CreateModelCommandBuilder setBurnInIterations(Integer burnInIterations) {
      this.burnInIterations = burnInIterations;
      return this;
    }

    public CreateModelCommandBuilder setInferenceIterations(Integer inferenceIterations) {
      this.inferenceIterations = inferenceIterations;
      return this;
    }

    public CreateModelCommandBuilder setThinningFactor(Integer thinningFactor) {
      this.thinningFactor = thinningFactor;
      return this;
    }

    public CreateModelCommandBuilder setLikelihood(String likelihood) {
      this.likelihood = likelihood;
      return this;
    }

    public CreateModelCommandBuilder setLink(String link) {
      this.link = link;
      return this;
    }

    public CreateModelCommandBuilder setOutcomeScale(Double outcomeScale) {
      this.outcomeScale = outcomeScale;
      return this;
    }

    public CreateModelCommandBuilder setRegressor(JSONObject regressor) {
      this.regressor = regressor;
      return this;
    }

    public CreateModelCommandBuilder setSensitivity(JSONObject sensitivity) {
      this.sensitivity = sensitivity;
      return this;
    }

    public CreateModelCommand build() {
      return new CreateModelCommand(this);
    }
  }
}

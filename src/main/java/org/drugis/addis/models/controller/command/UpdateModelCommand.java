package org.drugis.addis.models.controller.command;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by connor on 07/10/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateModelCommand extends CreateModelCommand {

  private Integer id;

  public UpdateModelCommand() {
    super();
  }

  public UpdateModelCommand(UpdateModelCommandBuilder builder) {
    super(new CreateModelCommandBuilder()
            .setTitle(builder.getTitle())
            .setLinearModel(builder.getLinearModel())
            .setModelType(builder.getModelType())
            .setHeterogeneityPriorCommand(builder.getHeterogeneityPriorCommand())
            .setBurnInIterations(builder.getBurnInIterations())
            .setInferenceIterations(builder.getInferenceIterations())
            .setThinningFactor(builder.getThinningFactor())
            .setLikelihood(builder.getLikelihood())
            .setLink(builder.getLink())
            .setOutcomeScale(builder.getOutcomeScale()));

    this.id = builder.id;
  }

  public Integer getId() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof UpdateModelCommand)) return false;
    if (!super.equals(o)) return false;

    UpdateModelCommand that = (UpdateModelCommand) o;

    return id.equals(that.id);

  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + id.hashCode();
    return result;
  }

  public static class UpdateModelCommandBuilder extends CreateModelCommand.CreateModelCommandBuilder {
    private Integer id;
    private String title;
    private String linearModel;
    private ModelTypeCommand modelTypeCommand;
    private Integer burnInIterations;
    private Integer inferenceIterations;
    private Integer thinningFactor;
    private String likelihood;
    private String link;
    private HeterogeneityPriorCommand heterogeneityPriorCommand;
    private Double outcomeScale;

    public UpdateModelCommandBuilder setId(Integer id) {
      this.id = id;
      return this;
    }

    public UpdateModelCommandBuilder setModelTypeCommand(ModelTypeCommand modelTypeCommand) {
      this.modelTypeCommand = modelTypeCommand;
      return this;
    }

    public String getTitle() {
      return title;
    }

    public UpdateModelCommandBuilder setTitle(String modelTitle) {
      this.title = modelTitle;
      return this;
    }

    public String getLinearModel() {
      return linearModel;
    }

    public UpdateModelCommandBuilder setLinearModel(String linearModel) {
      this.linearModel = linearModel;
      return this;
    }

    public ModelTypeCommand getModelType() {
      return modelTypeCommand;
    }

    public HeterogeneityPriorCommand getHeterogeneityPriorCommand() {
      return heterogeneityPriorCommand;
    }

    public Integer getBurnInIterations() {
      return burnInIterations;
    }

    public UpdateModelCommandBuilder setBurnInIterations(Integer burnInIterations) {
      this.burnInIterations = burnInIterations;
      return this;
    }

    public Integer getInferenceIterations() {
      return inferenceIterations;
    }

    public UpdateModelCommandBuilder setInferenceIterations(Integer inferenceIterations) {
      this.inferenceIterations = inferenceIterations;
      return this;
    }

    public Integer getThinningFactor() {
      return thinningFactor;
    }

    public UpdateModelCommandBuilder setThinningFactor(Integer thinningFactor) {
      this.thinningFactor = thinningFactor;
      return this;
    }

    public String getLikelihood() {
      return likelihood;
    }

    public UpdateModelCommandBuilder setLikelihood(String likelihood) {
      this.likelihood = likelihood;
      return this;
    }

    public String getLink() {
      return link;
    }

    public UpdateModelCommandBuilder setLink(String link) {
      this.link = link;
      return this;
    }

    public Double getOutcomeScale() {
      return outcomeScale;
    }

    public UpdateModelCommand build() {
      return new UpdateModelCommand(this);
    }
  }
}

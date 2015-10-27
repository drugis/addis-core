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

  public UpdateModelCommand(Integer id, String title, String linearModel, ModelTypeCommand modelType, HeterogeneityPriorCommand heterogeneityPriorCommand, Integer burnInIterations, Integer inferenceIterations, Integer thinningFactor, String likelihood, String link, Double outcomeScale) {
    super(title, linearModel, modelType, heterogeneityPriorCommand, burnInIterations, inferenceIterations, thinningFactor, likelihood, link, outcomeScale);
    this.id = id;
  }

  public UpdateModelCommand(Integer id, String title, String linearModel, ModelTypeCommand modelType, Integer burnInIterations, Integer inferenceIterations, Integer thinningFactor, String likelihood, String link) {
    super(title, linearModel, modelType, burnInIterations, inferenceIterations, thinningFactor, likelihood, link);
    this.id = id;
  }

  public UpdateModelCommand(Integer id, String title, String linearModel, ModelTypeCommand modelType, Integer burnInIterations, Integer inferenceIterations, Integer thinningFactor, String likelihood, String link, Double outcomeScale) {
    super(title, linearModel, modelType, burnInIterations, inferenceIterations, thinningFactor, likelihood, link, outcomeScale);
    this.id = id;
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
}

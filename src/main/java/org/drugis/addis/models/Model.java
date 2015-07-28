package org.drugis.addis.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.commons.lang3.tuple.Pair;
import org.drugis.addis.models.exceptions.InvalidModelTypeException;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Created by daan on 22-5-14.
 */
@Entity
public class Model {

  public final static String NETWORK_MODEL_TYPE = "network";
  public final static String PAIRWISE_MODEL_TYPE = "pairwise";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private Integer taskId;
  private Integer analysisId;
  private String title;
  private String linearModel;
  private String modelType;
  private Integer burnInIterations;
  private Integer inferenceIterations;
  private Integer thinningFactor;

  public Model() {
  }

  public Model(Integer taskId, Integer id, Integer analysisId, String title, String linearModel, String modelType, Integer burnInIterations, Integer inferenceIterations, Integer thinningFactor) {
    this.taskId = taskId;
    this.id = id;
    this.analysisId = analysisId;
    this.title = title;
    this.linearModel = linearModel;
    this.modelType = modelType;
    this.burnInIterations = burnInIterations;
    this.inferenceIterations = inferenceIterations;
    this.thinningFactor = thinningFactor;
  }

  public Model(Integer id, Integer analysisId, String title, String linearModel, String modelType, Integer burnInIterations, Integer inferenceIterations, Integer thinningFactor) {
    this(null, id, analysisId, title, linearModel, modelType, burnInIterations, inferenceIterations, thinningFactor);
  }

  public Model(Integer analysisId, String title, String linearModel, String modelType, String from, String to, Integer burnInIterations, Integer inferenceIterations, Integer thinningFactor) throws InvalidModelTypeException {
    this(null, null, analysisId, title, linearModel, modelType, burnInIterations, inferenceIterations, thinningFactor);

    if(Model.PAIRWISE_MODEL_TYPE.equals(modelType)) {
      this.modelType = String.format("{'type': '%s', 'details': {'from': '%s', 'to': '%s'}}", modelType, from, to);
    } else if (Model.NETWORK_MODEL_TYPE.equals(modelType)){
      this.modelType = String.format("{'type': '%s'}", modelType);
    }else {
      throw new InvalidModelTypeException("not a valid model type");
    }
  }

  public Integer getId() {
    return id;
  }

  public Integer getAnalysisId() {
    return analysisId;
  }

  public Integer getTaskId() {
    return taskId;
  }

  public String getTitle() {
    return title;
  }

  public String getLinearModel() {
    return linearModel;
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

  @JsonIgnore
  public String getModelTypeTypeAsString() {
    JSONObject jsonObject = (JSONObject) JSONValue.parse(modelType);
    return (String) jsonObject.get("type");
  }

  public ModelType getModelType() {
    Pair<String, String> typeDetails = getPairwiseDetails();
    TypeDetails details = null;
    if(typeDetails != null) {
       details = new TypeDetails(typeDetails.getLeft(), typeDetails.getRight());
    }
    return new ModelType(getModelTypeTypeAsString(), details);
  }

  @JsonIgnore
  public Pair<String, String> getPairwiseDetails() {
    if(PAIRWISE_MODEL_TYPE.equals(getModelTypeTypeAsString())){
      JSONObject jsonObject = (JSONObject) JSONValue.parse(modelType);
      JSONObject pairwiseDetails = (JSONObject) jsonObject.get("details");
      String to = (String) pairwiseDetails.get("to");
      String from = (String) pairwiseDetails.get("from");
      return Pair.of(from, to);
    }
    else {
      return null;
    }
  }

  public void setTaskId(Integer taskId) {
    this.taskId = taskId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Model model = (Model) o;

    if (taskId != null ? !taskId.equals(model.taskId) : model.taskId != null) return false;
    if (id != null ? !id.equals(model.id) : model.id != null) return false;
    if (!analysisId.equals(model.analysisId)) return false;
    if (!title.equals(model.title)) return false;
    if (!linearModel.equals(model.linearModel)) return false;
    if (!modelType.equals(model.modelType)) return false;
    if (!burnInIterations.equals(model.burnInIterations)) return false;
    if (!inferenceIterations.equals(model.inferenceIterations)) return false;
    return thinningFactor.equals(model.thinningFactor);

  }

  @Override
  public int hashCode() {
    int result = taskId != null ? taskId.hashCode() : 0;
    result = 31 * result + (id != null ? id.hashCode() : 0);
    result = 31 * result + analysisId.hashCode();
    result = 31 * result + title.hashCode();
    result = 31 * result + linearModel.hashCode();
    result = 31 * result + modelType.hashCode();
    result = 31 * result + burnInIterations.hashCode();
    result = 31 * result + inferenceIterations.hashCode();
    result = 31 * result + thinningFactor.hashCode();
    return result;
  }

  public class ModelType {
    private String type;
    private TypeDetails details;

    public ModelType() {
    }

    public ModelType(String type, TypeDetails details) {
      this.type = type;
      this.details = details;
    }

    public String getType() {
      return type;
    }

    public TypeDetails getDetails() {
      return details;
    }
  }

  public class TypeDetails {
    String to;
    String from;

    public TypeDetails() {
    }

    public TypeDetails(String from, String to) {
      this.to = to;
      this.from = from;
    }

    public String getTo() {
      return to;
    }

    public String getFrom() {
      return from;
    }
  }
}

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
  private Integer analysisId;
  private String title;
  private String linearModel;
  private String modelType;

  private Integer taskId;

  public Model() {
  }

  public Model(Integer taskId, Integer id, Integer analysisId, String title, String linearModel, String modelType) {
    this.taskId = taskId;
    this.id = id;
    this.analysisId = analysisId;
    this.title = title;
    this.linearModel = linearModel;
    this.modelType = modelType;
  }

  public Model(Integer id, Integer analysisId, String title, String linearModel, String modelType) {
    this(null, id, analysisId, title, linearModel, modelType);
  }

  public Model(Integer analysisId, String title, String linearModel, String modelType, String from, String to) throws InvalidModelTypeException {
    this(null, null, analysisId, title, linearModel, modelType);

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

    if (!id.equals(model.id)) return false;
    if (!analysisId.equals(model.analysisId)) return false;
    if (!title.equals(model.title)) return false;
    if (!linearModel.equals(model.linearModel)) return false;
    if (!modelType.equals(model.modelType)) return false;
    return !(taskId != null ? !taskId.equals(model.taskId) : model.taskId != null);

  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + analysisId.hashCode();
    result = 31 * result + title.hashCode();
    result = 31 * result + linearModel.hashCode();
    result = 31 * result + modelType.hashCode();
    result = 31 * result + (taskId != null ? taskId.hashCode() : 0);
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

package org.drugis.addis.models;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import net.minidev.json.parser.JSONParser;
import org.apache.commons.lang3.tuple.Pair;

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

  public Model(Integer analysisId, String title, String linearModel, String modelType) {
    this(null, analysisId, title, linearModel, modelType);
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

  public String getModelType() {
    JSONObject jsonObject = (JSONObject) JSONValue.parse(modelType);
    return (String) jsonObject.get("type");
  }

  public Pair<String, String> getPairwiseDetials() {
    if(PAIRWISE_MODEL_TYPE.equals(getModelType())){
      JSONObject jsonObject = (JSONObject) JSONValue.parse(modelType);
      JSONObject pairwiseDetials = (JSONObject) jsonObject.get("details");
      String to = (String) pairwiseDetials.get("to");
      String from = (String) pairwiseDetials.get("from");
      return Pair.of(to, from);
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
}

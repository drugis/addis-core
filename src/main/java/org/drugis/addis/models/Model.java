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
  public final static String NODE_SPLITTING_MODEL_TYPE = "node-split";
  public final static String LINEAR_MODEL_FIXED = "fixed";
  public final static String LINEAR_MODEL_RANDOM = "random";

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

  private Model(ModelBuilder builder) throws InvalidModelTypeException {
    this.id = builder.id;
    this.taskId = builder.taskId;
    this.analysisId = builder.analysisId;
    this.title = builder.title;
    this.linearModel = builder.linearModel;
    this.burnInIterations = builder.burnInIterations;
    this.inferenceIterations = builder.inferenceIterations;
    this.thinningFactor = builder.thinningFactor;

    if (Model.PAIRWISE_MODEL_TYPE.equals(builder.modelType) || Model.NODE_SPLITTING_MODEL_TYPE.equals(builder.modelType)) {
      this.modelType = String.format("{'type': '%s', 'details': {'from': {'id' : %s, 'name': '%s'}, 'to': {'id': %s, 'name': '%s'}}}",
              builder.modelType, builder.from.getId(), builder.from.getName(), builder.to.getId(), builder.to.getName());
    } else if (Model.NETWORK_MODEL_TYPE.equals(builder.modelType)) {
      this.modelType = String.format("{'type': '%s'}", builder.modelType);
    } else {
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
    Pair<Model.DetailNode, Model.DetailNode> typeDetails = getPairwiseDetails();
    TypeDetails details = null;
    if (typeDetails != null) {
      details = new TypeDetails(typeDetails.getLeft(), typeDetails.getRight());
    }
    return new ModelType(getModelTypeTypeAsString(), details);
  }

  @JsonIgnore
  public Pair<Model.DetailNode, Model.DetailNode> getPairwiseDetails() {
    if (PAIRWISE_MODEL_TYPE.equals(getModelTypeTypeAsString())
            || NODE_SPLITTING_MODEL_TYPE.equals(getModelTypeTypeAsString())) {
      JSONObject jsonObject = (JSONObject) JSONValue.parse(modelType);
      JSONObject pairwiseDetails = (JSONObject) jsonObject.get("details");
      JSONObject from = (JSONObject) pairwiseDetails.get("from");
      JSONObject to = (JSONObject) pairwiseDetails.get("to");
      Integer fromId = (Integer) from.get("id");
      String fromName = (String) from.get("name");
      Integer toId = (Integer) to.get("id");
      String toName = (String) to.get("name");
      return Pair.of(new DetailNode(fromId, fromName), new DetailNode(toId, toName));
    } else {
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
    DetailNode from;
    DetailNode to;

    public TypeDetails() {
    }

    public TypeDetails(DetailNode from, DetailNode to) {
      this.from = from;
      this.to = to;
    }

    public DetailNode getFrom() {
      return from;
    }

    public DetailNode getTo() {
      return to;
    }
  }

  public static class DetailNode {
    Integer id;
    String name;

    public DetailNode() {
    }

    public DetailNode(Integer id, String name) {
      this.id = id;
      this.name = name;
    }

    public Integer getId() {
      return id;
    }

    public String getName() {
      return name;
    }
  }


  public static class ModelBuilder {
    private Integer taskId = null;
    private Integer id;
    private Integer analysisId;
    private String title;
    private String linearModel;
    private String modelType;
    private Integer burnInIterations;
    private Integer inferenceIterations;
    private Integer thinningFactor;
    private DetailNode from;
    private DetailNode to;

    public ModelBuilder taskId(Integer taskId) {
      this.taskId = taskId;
      return this;
    }

    public ModelBuilder id(Integer id) {
      this.id = id;
      return this;
    }

    public ModelBuilder analysisId(Integer analysisId) {
      this.analysisId = analysisId;
      return this;
    }

    public ModelBuilder title(String title) {
      this.title = title;
      return this;
    }

    public ModelBuilder linearModel(String linearModel) {
      this.linearModel = linearModel;
      return this;
    }

    public ModelBuilder modelType(String modelType) {
      this.modelType = modelType;
      return this;
    }

    public ModelBuilder burnInIterations(Integer burnInIterations) {
      this.burnInIterations = burnInIterations;
      return this;
    }

    public ModelBuilder inferenceIterations(Integer inferenceIterations) {
      this.inferenceIterations = inferenceIterations;
      return this;
    }

    public ModelBuilder thinningFactor(Integer thinningFactor) {
      this.thinningFactor = thinningFactor;
      return this;
    }

    public ModelBuilder from(DetailNode from) {
      this.from = from;
      return this;
    }

    public ModelBuilder to(DetailNode to) {
      this.to = to;
      return this;
    }

    public Model build() throws InvalidModelTypeException {
      return new Model(this);
    }
  }
}

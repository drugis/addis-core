package org.drugis.addis.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.commons.lang3.tuple.Pair;
import org.drugis.addis.exception.OperationNotPermittedException;
import org.drugis.addis.models.exceptions.InvalidModelException;
import org.drugis.addis.util.JSONObjectConverter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.net.URI;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Entity
public class Model {

  public final static String NETWORK_MODEL_TYPE = "network";
  public final static String PAIRWISE_MODEL_TYPE = "pairwise";
  public final static String NODE_SPLITTING_MODEL_TYPE = "node-split";
  public final static String REGRESSION_MODEL_TYPE = "regression";
  public final static String LINEAR_MODEL_FIXED = "fixed";
  public final static String LINEAR_MODEL_RANDOM = "random";

  public final static String LIKELIHOOD_NORMAL = "normal";
  public final static String LIKELIHOOD_BINOM = "binom";
  public final static String LIKELIHOOD_POISSON = "poisson";

  public final static String LINK_IDENTITY = "identity";
  public final static String LINK_LOGIT = "logit";
  public final static String LINK_LOG = "log";
  public final static String LINK_CLOGLOG = "cloglog";
  public final static String LINK_SMD = "smd";

  public final static List<String> LINK_OPTIONS = Arrays.asList(
          LINK_IDENTITY,
          LINK_LOGIT,
          LINK_LOG,
          LINK_CLOGLOG,
          LINK_SMD
  );

  public final static String STD_DEV_HETEROGENEITY_PRIOR_TYPE = "standard-deviation";
  public final static String VARIANCE_HETEROGENEITY_PRIOR_TYPE = "variance";
  public final static String PRECISION_HETEROGENEITY_PRIOR_TYPE = "precision";
  @Transient
  private String runStatus;
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;
  private String taskUrl;
  private Integer analysisId;
  private String title;
  private String linearModel;
  private String modelType;
  private String heterogeneityPrior;
  private Integer burnInIterations;
  private Integer inferenceIterations;
  private Integer thinningFactor;
  private String likelihood;
  private String link;
  private Double outcomeScale;
  @Convert(converter = JSONObjectConverter.class)
  private JSONObject regressor;
  @Convert(converter = JSONObjectConverter.class)
  private JSONObject sensitivity;

  private Boolean archived;

  @Column(name = "archived_on")
  @Type(type = "date")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  private Date archivedOn;

  @OneToOne
  @PrimaryKeyJoinColumn(referencedColumnName = "modelId")
  private ModelBaseline baseline;

  public Model() {
  }

  public Model(Model other) {
    this.analysisId = other.analysisId;
    this.title = other.title;
    this.linearModel = other.linearModel;
    this.modelType = other.modelType;
    this.heterogeneityPrior = other.heterogeneityPrior;
    this.burnInIterations = other.burnInIterations;
    this.inferenceIterations = other.inferenceIterations;
    this.thinningFactor = other.thinningFactor;
    this.likelihood = other.likelihood;
    this.link = other.link;
    this.outcomeScale = other.outcomeScale;
    this.regressor = other.regressor;
    this.sensitivity = other.sensitivity;
    this.archived = false;
    this.baseline = other.baseline;
  }

  private Model(ModelBuilder builder) throws InvalidModelException {
    this.id = builder.id;
    if (builder.taskUri != null) {
      this.taskUrl = builder.taskUri.toString();
    }
    this.analysisId = builder.analysisId;
    this.title = builder.title;
    this.linearModel = builder.linearModel;
    this.burnInIterations = builder.burnInIterations;
    this.inferenceIterations = builder.inferenceIterations;
    this.thinningFactor = builder.thinningFactor;
    this.likelihood = builder.likelihood;
    this.link = builder.link;

    if (!LINK_OPTIONS.contains(this.link)) {
      throw new InvalidModelException(this.link + " is not a valid link type");
    }
    this.outcomeScale = builder.outcomeScale;
    this.regressor = builder.regressor;
    this.sensitivity = builder.sensitivity;

    if (Model.PAIRWISE_MODEL_TYPE.equals(builder.modelType) || Model.NODE_SPLITTING_MODEL_TYPE.equals(builder.modelType)) {
      this.modelType = String.format("{'type': '%s', 'details': {'from': {'id' : %s, 'name': '%s'}, 'to': {'id': %s, 'name': '%s'}}}",
              builder.modelType, builder.from.getId(), builder.from.getName(), builder.to.getId(), builder.to.getName());
    } else if (Model.NETWORK_MODEL_TYPE.equals(builder.modelType) || Model.REGRESSION_MODEL_TYPE.equals(builder.modelType)) {
      this.modelType = String.format("{'type': '%s'}", builder.modelType);
    } else {
      throw new InvalidModelException("not a valid model type");
    }
    if (builder.heterogeneityPriorType == null) {
      this.heterogeneityPrior = null;
    } else if (Model.STD_DEV_HETEROGENEITY_PRIOR_TYPE.equals(builder.heterogeneityPriorType)) {
      this.heterogeneityPrior = String.format("{'type': '%s', values: {'lower': %s, 'upper': %s} }", builder.heterogeneityPriorType, builder.lower, builder.upper);
    } else if (Model.VARIANCE_HETEROGENEITY_PRIOR_TYPE.equals(builder.heterogeneityPriorType)) {
      this.heterogeneityPrior = String.format("{'type': '%s', values: {'mean': %s, 'stdDev': %s} }", builder.heterogeneityPriorType, builder.mean, builder.stdDev);
    } else if (Model.PRECISION_HETEROGENEITY_PRIOR_TYPE.equals(builder.heterogeneityPriorType)) {
      this.heterogeneityPrior = String.format("{'type': '%s', values: {'rate': %s, 'shape': %s} }", builder.heterogeneityPriorType, builder.rate, builder.shape);
    } else {
      throw new InvalidModelException("not a valid heterogeneity prior type");
    }

    this.archived = false;

  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getAnalysisId() {
    return analysisId;
  }

  public void setAnalysisId(Integer analysisId) {
    this.analysisId = analysisId;
  }

  public URI getTaskUrl() {
    return taskUrl != null ? URI.create(taskUrl) : null;
  }

  public void setTaskUrl(URI taskUrl) {
    this.taskUrl = taskUrl != null ? taskUrl.toString() : null;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getLinearModel() {
    return linearModel;
  }

  public Integer getBurnInIterations() {
    return burnInIterations;
  }

  public void setBurnInIterations(Integer burnInIterations) {
    this.burnInIterations = burnInIterations;
  }

  public Integer getInferenceIterations() {
    return inferenceIterations;
  }

  public void setInferenceIterations(Integer inferenceIterations) {
    this.inferenceIterations = inferenceIterations;
  }

  public Integer getThinningFactor() {
    return thinningFactor;
  }

  public void setThinningFactor(Integer thinningFactor) {
    this.thinningFactor = thinningFactor;
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

  public void setRegressor(JSONObject regressor) {
    this.regressor = regressor;
  }

  public JSONObject getSensitivity() {
    return sensitivity;
  }

  public String getRunStatus() {
    return runStatus;
  }

  public ModelBaseline getBaseline() {
    return baseline;
  }

  public void setRunStatus(String newStatus) {
    if (!"failed".equals(newStatus) && !"done".equals(newStatus) && !"progress".equals(newStatus) && !"unknown".equals(newStatus)) {
      throw new IllegalArgumentException("Unknown model run status: " + newStatus);
    }
    this.runStatus = newStatus;
  }

  public Boolean getArchived() {
    return archived;
  }

  public Date getArchivedOn() {
    return archivedOn;
  }

  public void setArchived(Boolean archived) {
    this.archived = archived;
  }

  public void setArchivedOn(Date archivedOn) {
    this.archivedOn = archivedOn;
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

  public HeterogeneityPrior getHeterogeneityPrior() {
    JSONObject heterogeneityObject = (JSONObject) JSONValue.parse(heterogeneityPrior);
    if (heterogeneityObject != null) {
      String priorType = (String) heterogeneityObject.get("type");
      HeterogeneityValues values = null;
      if (Model.STD_DEV_HETEROGENEITY_PRIOR_TYPE.equals(priorType)) {
        JSONObject values1 = (JSONObject) heterogeneityObject.get("values");
        Double lower = (Double) values1.get("lower");
        Double upper = (Double) values1.get("upper");
        values = new HeterogeneityStdDevValues(lower, upper);
      } else if (Model.VARIANCE_HETEROGENEITY_PRIOR_TYPE.equals(priorType)) {
        JSONObject values1 = (JSONObject) heterogeneityObject.get("values");
        Double mean = (Double) values1.get("mean");
        Double stdDev = (Double) values1.get("stdDev");
        values = new HeterogeneityVarianceValues(mean, stdDev);
      } else if (Model.PRECISION_HETEROGENEITY_PRIOR_TYPE.equals(priorType)) {
        JSONObject values1 = (JSONObject) heterogeneityObject.get("values");
        Double rate = (Double) values1.get("rate");
        Double shape = (Double) values1.get("shape");
        values = new HeterogeneityPrecisionValues(rate, shape);
      }
      return new HeterogeneityPrior(priorType, values);
    } else {
      return null;
    }
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Model model = (Model) o;
    return Objects.equals(runStatus, model.runStatus) &&
            Objects.equals(id, model.id) &&
            Objects.equals(taskUrl, model.taskUrl) &&
            Objects.equals(analysisId, model.analysisId) &&
            Objects.equals(title, model.title) &&
            Objects.equals(linearModel, model.linearModel) &&
            Objects.equals(modelType, model.modelType) &&
            Objects.equals(heterogeneityPrior, model.heterogeneityPrior) &&
            Objects.equals(burnInIterations, model.burnInIterations) &&
            Objects.equals(inferenceIterations, model.inferenceIterations) &&
            Objects.equals(thinningFactor, model.thinningFactor) &&
            Objects.equals(likelihood, model.likelihood) &&
            Objects.equals(link, model.link) &&
            Objects.equals(outcomeScale, model.outcomeScale) &&
            Objects.equals(regressor, model.regressor) &&
            Objects.equals(sensitivity, model.sensitivity) &&
            Objects.equals(archived, model.archived) &&
            Objects.equals(archivedOn, model.archivedOn) &&
            Objects.equals(baseline, model.baseline);
  }

  @Override
  public int hashCode() {

    return Objects.hash(runStatus, id, taskUrl, analysisId, title, linearModel, modelType, heterogeneityPrior, burnInIterations, inferenceIterations, thinningFactor, likelihood, link, outcomeScale, regressor, sensitivity, archived, archivedOn, baseline);
  }

  public void updateTypeDetails(Integer newFromId, Integer newToId) throws OperationNotPermittedException {
    if (!getModelTypeTypeAsString().equals("pairwise") && !getModelTypeTypeAsString().equals("node-split")) {
      throw new OperationNotPermittedException("Can only update typedetails for pairwise and node split models.");
    }
    Pair<DetailNode, DetailNode> pairwiseDetails = getPairwiseDetails();
    TypeDetails typeDetails = new TypeDetails(
            new DetailNode(newFromId, pairwiseDetails.getLeft().getName()),
            new DetailNode(newToId, pairwiseDetails.getRight().getName())
    );
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      this.modelType = objectMapper.writeValueAsString(new ModelType(getModelTypeTypeAsString(), typeDetails));
    } catch (JsonProcessingException e) {
      throw new OperationNotPermittedException(e.getMessage());
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
    private URI taskUri = null;
    private Integer id;
    private Integer analysisId;
    private String title;
    private String linearModel;
    private String modelType;
    private String heterogeneityPriorType;
    private Integer burnInIterations;
    private Integer inferenceIterations;
    private Integer thinningFactor;
    private DetailNode from;
    private DetailNode to;
    private String likelihood;
    private String link;
    private Double outcomeScale;
    private Double lower;
    private Double upper;
    private Double mean;
    private Double stdDev;
    private Double rate;
    private Double shape;
    private JSONObject regressor;
    private JSONObject sensitivity;

    public ModelBuilder(Integer analysisId, String title) {
      this.analysisId = analysisId;
      this.title = title;
    }

    public ModelBuilder taskUri(URI taskUri) {
      this.taskUri = taskUri;
      return this;
    }

    public ModelBuilder id(Integer id) {
      this.id = id;
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

    public ModelBuilder heterogeneityPriorType(String heterogeneityPriorType) {
      this.heterogeneityPriorType = heterogeneityPriorType;
      return this;
    }

    public ModelBuilder lower(Double lower) {
      this.lower = lower;
      return this;
    }

    public ModelBuilder upper(Double upper) {
      this.upper = upper;
      return this;
    }

    public ModelBuilder mean(Double mean) {
      this.mean = mean;
      return this;
    }

    public ModelBuilder stdDev(Double stdDev) {
      this.stdDev = stdDev;
      return this;
    }

    public ModelBuilder rate(Double rate) {
      this.rate = rate;
      return this;
    }

    public ModelBuilder shape(Double shape) {
      this.shape = shape;
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

    public ModelBuilder likelihood(String likelihood) {
      this.likelihood = likelihood;
      return this;
    }

    public ModelBuilder link(String link) {
      this.link = link;
      return this;
    }

    public ModelBuilder outcomeScale(Double outcomeScale) {
      this.outcomeScale = outcomeScale;
      return this;
    }

    public ModelBuilder regressor(JSONObject regressor) {
      this.regressor = regressor;
      return this;
    }

    public ModelBuilder sensitivity(JSONObject sensitivity) {
      this.sensitivity = sensitivity;
      return this;
    }

    public Model build() throws InvalidModelException {
      return new Model(this);
    }

  }

  public class HeterogeneityPrior {
    private String type;
    private HeterogeneityValues values;

    public HeterogeneityPrior() {
    }

    HeterogeneityPrior(String type, HeterogeneityValues values) {
      this.type = type;
      this.values = values;
    }

    public String getType() {
      return type;
    }

    public HeterogeneityValues getValues() {
      return values;
    }
  }

  abstract class HeterogeneityValues {
  }

  public class HeterogeneityStdDevValues extends HeterogeneityValues {
    private Double lower;
    private Double upper;

    public HeterogeneityStdDevValues() {
    }

    HeterogeneityStdDevValues(Double lower, Double upper) {
      this.lower = lower;
      this.upper = upper;
    }

    public Double getLower() {
      return lower;
    }

    public Double getUpper() {
      return upper;
    }
  }

  public class HeterogeneityVarianceValues extends HeterogeneityValues {
    private Double mean;
    private Double stdDev;

    public HeterogeneityVarianceValues() {
    }

    HeterogeneityVarianceValues(Double mean, Double stdDev) {
      this.mean = mean;
      this.stdDev = stdDev;
    }

    public Double getMean() {
      return mean;
    }

    public Double getStdDev() {
      return stdDev;
    }
  }

  public class HeterogeneityPrecisionValues extends HeterogeneityValues {
    private Double rate;
    private Double shape;

    public HeterogeneityPrecisionValues() {
    }

    HeterogeneityPrecisionValues(Double rate, Double shape) {
      this.rate = rate;
      this.shape = shape;
    }

    public Double getRate() {
      return rate;
    }

    public Double getShape() {
      return shape;
    }
  }

  public class ModelType {
    private String type;
    private TypeDetails details;

    public ModelType() {
    }

    ModelType(String type, TypeDetails details) {
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

    TypeDetails(DetailNode from, DetailNode to) {
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
}

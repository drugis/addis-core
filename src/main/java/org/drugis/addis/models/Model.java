package org.drugis.addis.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.apache.commons.lang3.tuple.Pair;
import org.drugis.addis.models.exceptions.InvalidModelException;
import org.drugis.addis.util.JSONObjectConverter;

import javax.persistence.*;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

;

/**
 * Created by daan on 22-5-14.
 */
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

  public final static List<String> LINK_OPTIONS = Arrays.asList(LINK_IDENTITY, LINK_LOGIT, LINK_LOG, LINK_CLOGLOG);

  public final static String STD_DEV_HETEROGENEITY_PRIOR_TYPE = "standard-deviation";
  public final static String VARIANCE_HETEROGENEITY_PRIOR_TYPE = "variance";
  public final static String PRECISION_HETEROGENEITY_PRIOR_TYPE = "precision";
  @Transient
  boolean hasResult = false;
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

  public Model() {
  }

  private Model(ModelBuilder builder) throws InvalidModelException {
    this.id = builder.id;
    this.taskUrl = builder.taskUri.toString();
    this.analysisId = builder.analysisId;
    this.title = builder.title;
    this.linearModel = builder.linearModel;
    this.burnInIterations = builder.burnInIterations;
    this.inferenceIterations = builder.inferenceIterations;
    this.thinningFactor = builder.thinningFactor;
    this.likelihood = builder.likelihood;
    this.link = builder.link;

    if(!LINK_OPTIONS.contains(this.link)){
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

  }

  public Integer getId() {
    return id;
  }

  public Integer getAnalysisId() {
    return analysisId;
  }

  public URI getTaskUrl() {
    return URI.create(taskUrl);
  }

  public void setTaskUrl(String taskUrl) {
    this.taskUrl = taskUrl;
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

  public JSONObject getSensitivity() {
    return sensitivity;
  }

  public boolean isHasResult() {
    return hasResult;
  }

  public void setHasResult() {
    this.hasResult = true;
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

    if (hasResult != model.hasResult) return false;
    if (id != null ? !id.equals(model.id) : model.id != null) return false;
    if (taskUrl != null ? !taskUrl.equals(model.taskUrl) : model.taskUrl != null) return false;
    if (!analysisId.equals(model.analysisId)) return false;
    if (!title.equals(model.title)) return false;
    if (linearModel != null ? !linearModel.equals(model.linearModel) : model.linearModel != null) return false;
    if (modelType != null ? !modelType.equals(model.modelType) : model.modelType != null) return false;
    if (heterogeneityPrior != null ? !heterogeneityPrior.equals(model.heterogeneityPrior) : model.heterogeneityPrior != null)
      return false;
    if (burnInIterations != null ? !burnInIterations.equals(model.burnInIterations) : model.burnInIterations != null)
      return false;
    if (inferenceIterations != null ? !inferenceIterations.equals(model.inferenceIterations) : model.inferenceIterations != null)
      return false;
    if (thinningFactor != null ? !thinningFactor.equals(model.thinningFactor) : model.thinningFactor != null)
      return false;
    if (likelihood != null ? !likelihood.equals(model.likelihood) : model.likelihood != null) return false;
    if (link != null ? !link.equals(model.link) : model.link != null) return false;
    if (outcomeScale != null ? !outcomeScale.equals(model.outcomeScale) : model.outcomeScale != null) return false;
    if (regressor != null ? !regressor.equals(model.regressor) : model.regressor != null) return false;
    return sensitivity != null ? sensitivity.equals(model.sensitivity) : model.sensitivity == null;

  }

  @Override
  public int hashCode() {
    int result = (hasResult ? 1 : 0);
    result = 31 * result + (id != null ? id.hashCode() : 0);
    result = 31 * result + (taskUrl != null ? taskUrl.hashCode() : 0);
    result = 31 * result + analysisId.hashCode();
    result = 31 * result + title.hashCode();
    result = 31 * result + (linearModel != null ? linearModel.hashCode() : 0);
    result = 31 * result + (modelType != null ? modelType.hashCode() : 0);
    result = 31 * result + (heterogeneityPrior != null ? heterogeneityPrior.hashCode() : 0);
    result = 31 * result + (burnInIterations != null ? burnInIterations.hashCode() : 0);
    result = 31 * result + (inferenceIterations != null ? inferenceIterations.hashCode() : 0);
    result = 31 * result + (thinningFactor != null ? thinningFactor.hashCode() : 0);
    result = 31 * result + (likelihood != null ? likelihood.hashCode() : 0);
    result = 31 * result + (link != null ? link.hashCode() : 0);
    result = 31 * result + (outcomeScale != null ? outcomeScale.hashCode() : 0);
    result = 31 * result + (regressor != null ? regressor.hashCode() : 0);
    result = 31 * result + (sensitivity != null ? sensitivity.hashCode() : 0);
    return result;
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

    public HeterogeneityPrior(String type, HeterogeneityValues values) {
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

  public abstract class HeterogeneityValues {
  }

  public class HeterogeneityStdDevValues extends HeterogeneityValues {
    private Double lower;
    private Double upper;

    public HeterogeneityStdDevValues() {
    }

    public HeterogeneityStdDevValues(Double lower, Double upper) {
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

    public HeterogeneityVarianceValues(Double mean, Double stdDev) {
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

    public HeterogeneityPrecisionValues(Double rate, Double shape) {
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
}

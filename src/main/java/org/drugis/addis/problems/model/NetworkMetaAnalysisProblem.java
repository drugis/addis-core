package org.drugis.addis.problems.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.drugis.addis.models.Model;
import org.drugis.addis.problems.model.problemEntry.AbstractProblemEntry;
import org.drugis.addis.problems.model.problemEntry.RelativeDataEntry;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class NetworkMetaAnalysisProblem extends AbstractProblem {

  protected List<AbstractProblemEntry> entries = new ArrayList<>();
  protected List<TreatmentEntry> treatments = new ArrayList<>();
  private RelativeEffectData relativeEffectData;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Map<String, Map<String, Double>> studyLevelCovariates;

  public NetworkMetaAnalysisProblem() {
  }

  public NetworkMetaAnalysisProblem(List<AbstractProblemEntry> entries,
                                    RelativeEffectData relativeEffectData,
                                    List<TreatmentEntry> treatments,
                                    Map<String, Map<String, Double>> studyLevelCovariates) {
    this.entries = entries;
    this.relativeEffectData = relativeEffectData;
    this.treatments = treatments;
    this.studyLevelCovariates = studyLevelCovariates;
  }

  public NetworkMetaAnalysisProblem(List<AbstractProblemEntry> entries,
                                    List<TreatmentEntry> treatments,
                                    Map<String, Map<String, Double>> studyLevelCovariates) {
    this(entries, new RelativeEffectData(), treatments, studyLevelCovariates);
  }

  public NetworkMetaAnalysisProblem(List<AbstractProblemEntry> entries, List<TreatmentEntry> treatments) {
    this(entries, new RelativeEffectData(), treatments, null);
  }

  public List<AbstractProblemEntry> getEntries() {
    return entries;
  }

  public RelativeEffectData getRelativeEffectData() {
    return relativeEffectData;
  }

  public List<TreatmentEntry> getTreatments() {
    return treatments;
  }

  public Map<String, Map<String, Double>> getStudyLevelCovariates() {
    return studyLevelCovariates;
  }

  public JSONObject buildProblemWithModelSettings(Model model, Integer preferredDirection) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    String problemString = objectMapper.writeValueAsString(this);
    JSONObject jsonProblem = new JSONObject(problemString);
    jsonProblem.put("linearModel", model.getLinearModel());
    jsonProblem.put("modelType", new JSONObject(objectMapper.writeValueAsString(model.getModelType())));
    jsonProblem.put("burnInIterations", model.getBurnInIterations());
    jsonProblem.put("inferenceIterations", model.getInferenceIterations());
    jsonProblem.put("thinningFactor", model.getThinningFactor());
    jsonProblem.put("likelihood", model.getLikelihood());
    jsonProblem.put("link", model.getLink());
    jsonProblem.put("regressor", model.getRegressor());
    jsonProblem.put("preferredDirection", preferredDirection);

    if (model.getHeterogeneityPrior() != null) {
      jsonProblem.put("heterogeneityPrior", new JSONObject(objectMapper.writeValueAsString(model.getHeterogeneityPrior())));
    }

    if (model.getSensitivity() != null) {
      jsonProblem.put("sensitivity", model.getSensitivity());
    }

    if (model.getOutcomeScale() != null) {
      jsonProblem.put("outcomeScale", model.getOutcomeScale());
    }
    return jsonProblem;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    NetworkMetaAnalysisProblem that = (NetworkMetaAnalysisProblem) o;
    return Objects.equals(entries, that.entries) &&
            Objects.equals(relativeEffectData, that.relativeEffectData) &&
            Objects.equals(treatments, that.treatments) &&
            Objects.equals(studyLevelCovariates, that.studyLevelCovariates);
  }

  @Override
  public int hashCode() {

    return Objects.hash(entries, relativeEffectData, treatments, studyLevelCovariates);
  }
}

package org.drugis.addis.problems.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.drugis.addis.models.Model;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by daan on 21-5-14.
 */
public class NetworkMetaAnalysisProblem extends AbstractProblem {

  protected List<AbstractNetworkMetaAnalysisProblemEntry> entries = new ArrayList<>();
  protected List<TreatmentEntry> treatments = new ArrayList<>();

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Map<String, Map<String, Double>> studyLevelCovariates;

  public NetworkMetaAnalysisProblem() {
  }

  public NetworkMetaAnalysisProblem(List<AbstractNetworkMetaAnalysisProblemEntry> entries,
                                    List<TreatmentEntry> treatments, Map<String, Map<String, Double>> studyLevelCovariates) {
    this.entries = entries;
    this.treatments = treatments;
    this.studyLevelCovariates = studyLevelCovariates;
  }

  public List<AbstractNetworkMetaAnalysisProblemEntry> getEntries() {
    return entries;
  }

  public List<TreatmentEntry> getTreatments() {
    return treatments;
  }

  public Map<String, Map<String, Double>> getStudyLevelCovariates() {
    return studyLevelCovariates;
  }

  public void setEntries(List<AbstractNetworkMetaAnalysisProblemEntry> entries) {
    this.entries = entries == null ? Collections.emptyList() : entries;
  }

  public JSONObject buildProblemWithModelSettings(Model model) throws JsonProcessingException {
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

    NetworkMetaAnalysisProblem problem = (NetworkMetaAnalysisProblem) o;

    if (!entries.equals(problem.entries)) return false;
    if (!treatments.equals(problem.treatments)) return false;
    return studyLevelCovariates.equals(problem.studyLevelCovariates);

  }

  @Override
  public int hashCode() {
    int result = entries.hashCode();
    result = 31 * result + treatments.hashCode();
    result = 31 * result + studyLevelCovariates.hashCode();
    return result;
  }

}

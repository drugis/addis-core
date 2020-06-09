package org.drugis.addis.problems.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import org.drugis.addis.analyses.model.BenefitRiskNMAOutcomeInclusion;
import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.models.Model;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.problems.model.*;
import org.drugis.addis.problems.service.NetworkBenefitRiskService;
import org.drugis.addis.problems.service.LinkService;
import org.drugis.addis.problems.service.NetworkBenefitRiskPerformanceEntryBuilder;
import org.drugis.addis.problems.service.NetworkMetaAnalysisService;
import org.drugis.addis.problems.service.model.AbstractMeasurementEntry;
import org.drugis.addis.projects.Project;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Service
public class NetworkBenefitRiskServiceImpl implements NetworkBenefitRiskService {

  @Inject
  private LinkService linkService;

  @Inject
  private NetworkMetaAnalysisService networkMetaAnalysisService;

  @Inject
  private NetworkBenefitRiskPerformanceEntryBuilder networkBenefitRiskPerformanceEntryBuilder;

  @Override
  public boolean hasBaseline(
          BenefitRiskNMAOutcomeInclusion inclusion,
          Map<Integer, Model> modelsById,
          Set<AbstractIntervention> includedInterventions
  ) {
    if (inclusion.getBaseline() == null) {
      return hasModelBaseline(inclusion, modelsById, includedInterventions);
    } else {
      return true;
    }
  }

  private boolean hasModelBaseline(
          BenefitRiskNMAOutcomeInclusion inclusion,
          Map<Integer, Model> modelsById,
          Set<AbstractIntervention> includedInterventions
  ) {
    if (inclusion.getModelId() == null) {
      return false;
    } else {
      return getBaseline(inclusion, modelsById, includedInterventions) != null;
    }
  }

  private String getBaseline(
          BenefitRiskNMAOutcomeInclusion inclusion,
          Map<Integer, Model> modelsById,
          Set<AbstractIntervention> includedInterventions
  ) {
    Model model = modelsById.get(inclusion.getModelId());
    if (model.getBaseline() == null) {
      return null;
    } else {
      return getModelBaseline(includedInterventions, model);
    }
  }

  private String getModelBaseline(
          Set<AbstractIntervention> includedInterventions,
          Model model
  ) {
    String modelBaseline = model.getBaseline().getBaseline();
    AbstractIntervention matchingIntervention = getInterventionMatchingBaseline(includedInterventions, modelBaseline);
    if (matchingIntervention == null) {
      return null;
    } else {
      return modelBaseline;
    }
  }

  private AbstractIntervention getInterventionMatchingBaseline(
          Set<AbstractIntervention> includedInterventions,
          String modelBaseline
  ) {
    JSONObject jsonBaseline = new JSONObject(modelBaseline);
    String baselineIntervention = jsonBaseline.getString("name");
    return includedInterventions.stream()
            .filter(intervention -> intervention.getName().equals(baselineIntervention))
            .findAny()
            .orElse(null);
  }

  @Override
  public boolean hasResults(Map<Integer, JsonNode> resultsByModelId, BenefitRiskNMAOutcomeInclusion inclusion) {
    return resultsByModelId.get(inclusion.getModelId()) != null;
  }

  @Override
  public NMAInclusionWithResults getNmaInclusionWithResults(
          Map<Integer, Outcome> outcomesById,
          Set<AbstractIntervention> includedInterventions,
          Map<Integer, Model> modelsById,
          Map<Integer, JsonNode> resultsByModelId,
          BenefitRiskNMAOutcomeInclusion inclusion
  ) {
    Outcome outcome = outcomesById.get(inclusion.getOutcomeId());
    Model model = modelsById.get(inclusion.getModelId());
    JsonNode pataviResults = resultsByModelId.get(inclusion.getModelId());
    if (inclusion.getBaseline() == null) {
      inclusion.setBaselineThroughString(model.getBaseline().getBaseline());
    }
    return new NMAInclusionWithResults(outcome, model, pataviResults, includedInterventions, inclusion.getBaseline());
  }

  @Override
  public BenefitRiskProblem getNetworkProblem(
          Project project,
          NMAInclusionWithResults inclusionWithResults
  ) {
    URI modelURI = linkService.getModelSourceLink(project, inclusionWithResults.getModel());

    final Map<URI, CriterionEntry> criteria = networkMetaAnalysisService.buildCriteriaForInclusion(inclusionWithResults, modelURI);
    final Map<String, AlternativeEntry> alternatives = networkMetaAnalysisService.buildAlternativesForInclusion(inclusionWithResults);
    final DataSourceEntry dataSourceEntry = getDataSourceEntry(criteria); // one criterion -> one datasource per NMA

    AbstractMeasurementEntry relativePerformance = networkBenefitRiskPerformanceEntryBuilder.build(inclusionWithResults, dataSourceEntry);
    return new BenefitRiskProblem(criteria, alternatives, Collections.singletonList(relativePerformance));
  }

  private DataSourceEntry getDataSourceEntry(Map<URI, CriterionEntry> criteria) {
    return criteria.values().iterator().next().getDataSources().get(0);
  }
}
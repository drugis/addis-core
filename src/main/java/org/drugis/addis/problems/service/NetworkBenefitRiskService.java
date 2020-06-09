package org.drugis.addis.problems.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.drugis.addis.analyses.model.BenefitRiskNMAOutcomeInclusion;
import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.models.Model;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.problems.model.BenefitRiskProblem;
import org.drugis.addis.problems.model.NMAInclusionWithResults;
import org.drugis.addis.projects.Project;

import java.util.Map;
import java.util.Set;

public interface NetworkBenefitRiskService {
  boolean hasBaseline(BenefitRiskNMAOutcomeInclusion inclusion, Map<Integer, Model> modelsById, Set<AbstractIntervention> includedInterventions);

  boolean hasResults(Map<Integer, JsonNode> resultsByModelId, BenefitRiskNMAOutcomeInclusion inclusion);

  NMAInclusionWithResults getNmaInclusionWithResults(Map<Integer, Outcome> outcomesById, Set<AbstractIntervention> includedInterventions, Map<Integer, Model> modelsById, Map<Integer, JsonNode> resultsByModelId, BenefitRiskNMAOutcomeInclusion inclusion);

  BenefitRiskProblem getNetworkProblem(Project project, NMAInclusionWithResults inclusion);
}

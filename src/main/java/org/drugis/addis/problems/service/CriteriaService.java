package org.drugis.addis.problems.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.drugis.addis.analyses.Analysis;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.problems.model.CriterionEntry;
import org.drugis.addis.problems.model.Variable;
import org.drugis.addis.projects.Project;
import org.drugis.addis.trialverse.model.MeasurementType;
import org.drugis.addis.trialverse.service.TrialverseService;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;

/**
 * Created by connor on 28/03/14.
 */
@Service
public class CriteriaService {
  @Inject
  private TriplestoreService triplestoreService;

  @Inject
  private TrialverseService trialverseService;

  private ObjectMapper mapper = new ObjectMapper();

  /**
   * Match outcomes selected in the analysis to variables from the project namespace in Trialverse.
   */
  public List<Pair<Variable, CriterionEntry>> createVariableCriteriaPairs(Project project, Analysis analysis) {
    Map<String, Outcome> outcomesByUri = new HashMap<>();

    for (Outcome outcome : analysis.getSelectedOutcomes()) {
      outcomesByUri.put(outcome.getSemanticOutcomeUri(), outcome);
    }

    Map<Long, String> trialverseVariables = triplestoreService.getTrialverseVariables(project.getTrialverseId(), analysis.getStudyId(), outcomesByUri.keySet());
    List<ObjectNode> jsonVariables = trialverseService.getVariablesByIds(trialverseVariables.keySet());
    System.out.println("DEBUG outcome ids : " + trialverseVariables);

    List<Pair<Variable, CriterionEntry>> variableCriteriaPairs = new ArrayList<>();

    for (ObjectNode variableJSONNode : jsonVariables) {
      Variable variable = mapper.convertValue(variableJSONNode, Variable.class);
      String outcomeUri = trialverseVariables.get(variable.getId());
      Outcome outcome = outcomesByUri.get(outcomeUri);
      CriterionEntry criterionEntry = createCriterionEntry(outcome, variable);
      Pair<Variable, CriterionEntry> pair = new ImmutablePair<>(variable, criterionEntry);
      variableCriteriaPairs.add(pair);
    }

    return variableCriteriaPairs;
  }

  private CriterionEntry createCriterionEntry(Outcome outcome, Variable variable) throws EnumConstantNotPresentException {
    List<Double> scale;
    switch (variable.getMeasurementType()) {
      case RATE:
        scale = Arrays.asList(0.0, 1.0);
        break;
      case CONTINUOUS:
        scale = Arrays.asList(null, null);
        break;
      case CATEGORICAL:
        throw new EnumConstantNotPresentException(MeasurementType.class, "Categorical endpoints/adverse events not allowed");
      default:
        throw new EnumConstantNotPresentException(MeasurementType.class, variable.getMeasurementType().toString());
    }
    // NB: partialvaluefunctions to be filled in by MCDA component, left null here
    return new CriterionEntry(outcome.getName(), scale, null);
  }
}

package org.drugis.addis.problems.service.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.drugis.addis.analyses.Analysis;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.Intervention;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.problems.model.*;
import org.drugis.addis.problems.service.ProblemService;
import org.drugis.addis.projects.Project;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.trialverse.model.MeasurementType;
import org.drugis.addis.trialverse.service.TrialverseService;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;

/**
 * Created by daan on 3/21/14.
 */
@Service
public class ProblemServiceImpl implements ProblemService {

  @Inject
  AnalysisRepository analysisRepository;

  @Inject
  TriplestoreService triplestoreService;


  @Inject
  TrialverseService trialverseService;

  @Inject
  ProjectRepository projectRepository;

  @Override
  public Problem getProblem(Integer projectId, Integer analysisId) throws ResourceDoesNotExistException {
    Analysis analysis = analysisRepository.get(projectId, analysisId);
    Project project = projectRepository.getProjectById(projectId);

    List<String> interventionUris = new ArrayList<>(analysis.getSelectedInterventions().size());
    for (Intervention intervention : analysis.getSelectedInterventions()) {
      interventionUris.add(intervention.getSemanticInterventionUri());
    }
    List<String> outcomeUris = new ArrayList<>(analysis.getSelectedOutcomes().size());
    for (Outcome outcome : analysis.getSelectedOutcomes()) {
      outcomeUris.add(outcome.getSemanticOutcomeUri());
    }

    List<Long> drugIds = triplestoreService.getTrialverseDrugIds(project.getTrialverseId(), analysis.getStudyId(), interventionUris);
    System.out.println("DEBUG drug ids : " + drugIds);
    List<Long> outcomeIds = triplestoreService.getTrialverseOutcomeIds(project.getTrialverseId(), analysis.getStudyId(), outcomeUris);
    System.out.println("DEBUG outcome ids : " + outcomeIds);

    List<ObjectNode> jsonArms = trialverseService.getArmsByDrugIds(analysis.getStudyId(), drugIds);
    List<ObjectNode> jsonVariables = trialverseService.getVariablesByOutcomeIds(outcomeIds);
    List<ObjectNode> jsonMeasurements = trialverseService.getMeasurements(analysis.getStudyId(), outcomeIds);

    ObjectMapper mapper = new ObjectMapper();
    Map<String, AlternativeEntry> alternatives = new HashMap<>();
    for (ObjectNode jsonArm : jsonArms) {
      Arm arm = mapper.convertValue(jsonArm, Arm.class);
      alternatives.put(createKey(arm.getName()), new AlternativeEntry(arm.getName()));
    }

    Map<String, CriterionEntry> criteria = new HashMap<>();
    for (ObjectNode variableJSONNode : jsonVariables) {
      Variable variable = mapper.convertValue(variableJSONNode, Variable.class);
      criteria.put(createKey(variable.getName()), createCriterionEntry(variable));
    }

    return new Problem(analysis.getName(), alternatives, criteria);
  }

  private CriterionEntry createCriterionEntry(Variable variable) throws EnumConstantNotPresentException {
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
    return new CriterionEntry(variable.getName(), scale, null);
  }

  private String createKey(String value) {
    //TODO: Expand properly
    return value.replace(" ", "-").replace("/", "").toLowerCase();
  }
}

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
import org.drugis.addis.problems.service.model.AbstractMeasurementEntry;
import org.drugis.addis.projects.Project;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.trialverse.model.MeasurementType;
import org.drugis.addis.trialverse.service.TrialverseService;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.drugis.addis.util.JSONUtils;
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
    ObjectMapper mapper = new ObjectMapper();
    Analysis analysis = analysisRepository.get(projectId, analysisId);
    Project project = projectRepository.getProjectById(projectId);

    Map<String, Intervention> interventionsByUri = new HashMap<>();
    for (Intervention intervention : analysis.getSelectedInterventions()) {
      interventionsByUri.put(intervention.getSemanticInterventionUri(), intervention);
    }

    Map<Long, String> drugs = triplestoreService.getTrialverseDrugs(project.getTrialverseId(), analysis.getStudyId(), interventionsByUri.keySet());
    System.out.println("DEBUG drug ids : " + drugs);
    List<ObjectNode> jsonArms = trialverseService.getArmsByDrugIds(analysis.getStudyId(), drugs.keySet());
    Map<String, AlternativeEntry> alternatives = new HashMap<>();
    Map<Long, AlternativeEntry> alternativesCache = new HashMap<>();
    for (ObjectNode jsonArm : jsonArms) {
      Arm arm = mapper.convertValue(jsonArm, Arm.class);
      String drugUUID = drugs.get(arm.getDrugId());
      Intervention intervention = interventionsByUri.get(drugUUID);
      AlternativeEntry alternativeEntry = new AlternativeEntry(intervention.getName());
      alternativesCache.put(arm.getId(), alternativeEntry);
      alternatives.put(JSONUtils.createKey(alternativeEntry.getTitle()), alternativeEntry);
    }

    Map<String, Outcome> outcomesByUri = new HashMap<>();
    for (Outcome outcome : analysis.getSelectedOutcomes()) {
      outcomesByUri.put(outcome.getSemanticOutcomeUri(), outcome);
    }
    Map<Long, String> trialverseVariables = triplestoreService.getTrialverseVariables(project.getTrialverseId(), analysis.getStudyId(), outcomesByUri.keySet());
    System.out.println("DEBUG outcome ids : " + trialverseVariables);
    List<ObjectNode> jsonVariables = trialverseService.getVariablesByIds(trialverseVariables.keySet());
    Map<String, CriterionEntry> criteria = new HashMap<>();
    Map<Long, CriterionEntry> criteriaCache = new HashMap<>();
    for (ObjectNode variableJSONNode : jsonVariables) {
      Variable variable = mapper.convertValue(variableJSONNode, Variable.class);
      String outcomeUUID = trialverseVariables.get(variable.getId());
      Outcome outcome = outcomesByUri.get(outcomeUUID);
      CriterionEntry criterionEntry = createCriterionEntry(outcome, variable);
      criteriaCache.put(variable.getId(), criterionEntry);
      criteria.put(JSONUtils.createKey(variable.getName()), criterionEntry);
    }

    List<ObjectNode> jsonMeasurements = trialverseService.getOrderedMeasurements(analysis.getStudyId(), trialverseVariables.keySet(), alternativesCache.keySet());
    System.out.println("DEBUG jsonMeasurements : " + jsonMeasurements);
    List<Measurement> measurements = new ArrayList<>(jsonMeasurements.size());
    for (ObjectNode measurementJSONNode : jsonMeasurements) {
      Measurement measurement = mapper.convertValue(measurementJSONNode, Measurement.class);
      measurements.add(measurement);
    }

    PerformanceTableBuilder builder = new PerformanceTableBuilder(criteriaCache, alternativesCache, measurements);

    List<AbstractMeasurementEntry> performanceTable = builder.build();

    return new Problem(analysis.getName(), alternatives, criteria, performanceTable);
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

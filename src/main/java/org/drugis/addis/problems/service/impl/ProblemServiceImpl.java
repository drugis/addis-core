package org.drugis.addis.problems.service.impl;

import org.drugis.addis.analyses.Analysis;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.Intervention;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.problems.AlternativeEntry;
import org.drugis.addis.problems.CriterionEntry;
import org.drugis.addis.problems.Problem;
import org.drugis.addis.problems.service.ProblemService;
import org.drugis.addis.projects.Project;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.trialverse.model.Variable;
import org.drugis.addis.trialverse.repository.TrialverseRepository;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
  TrialverseRepository trialverseRepository;

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

    List<Long> drugIds = triplestoreService.getTrialverseDrugIds(project.getTrialverseId(), analysis.getStudyId(), interventionUris);

    System.out.println("DEBUG drug ids : " + drugIds);
    List<String> armNames = trialverseRepository.getArmNamesByDrugIds(analysis.getStudyId(), drugIds);

    Map<String, AlternativeEntry> alternatives = new HashMap<>();
    for (String armName : armNames) {
      alternatives.put(createKey(armName), new AlternativeEntry(armName));
    }

    List<String> outcomeUris = new ArrayList<>(analysis.getSelectedOutcomes().size());
    for (Outcome outcome : analysis.getSelectedOutcomes()) {
      outcomeUris.add(outcome.getSemanticOutcomeUri());
    }

    List<Long> outcomeIds = triplestoreService.getTrialverseOutcomeIds(project.getTrialverseId(), analysis.getStudyId(), outcomeUris);
    System.out.println("DEBUG outcome ids : " + outcomeIds);
    List<org.drugis.addis.trialverse.model.Variable> variables = trialverseRepository.getVariablesByOutcomeIds(outcomeIds);
    Map<String, CriterionEntry> criteria = new HashMap<>();
    for (org.drugis.addis.trialverse.model.Variable variable : variables) {
      criteria.put(createKey(variable.getName()), createCriterionEntry(variable));
    }
    return new Problem(analysis.getName(), alternatives, criteria);
  }

  private CriterionEntry createCriterionEntry(Variable variable) {
    //TODO replace them nulls
    return new CriterionEntry(variable.getName(), null, null);
  }

  private String createKey(String value) {
    //TODO: Expand properly
    return value.replace(" ", "-").replace("/", "").toLowerCase();
  }
}

package org.drugis.addis.problems.service.impl;

import org.drugis.addis.analyses.Analysis;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.Intervention;
import org.drugis.addis.problems.AlternativeEntry;
import org.drugis.addis.problems.Problem;
import org.drugis.addis.problems.service.ProblemService;
import org.drugis.addis.projects.Project;
import org.drugis.addis.projects.repository.ProjectRepository;
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

    List<Integer> drugIds = triplestoreService.getTrialverseDrugIds(project.getTrialverseId(), analysis.getStudyId(), interventionUris);

    System.out.println(drugIds);
    List<String> armNames = trialverseRepository.getArmNamesByDrugIds(analysis.getStudyId(), drugIds);

    Map<String, AlternativeEntry> alternatives = new HashMap<>();
    for (String armName : armNames) {
      alternatives.put(createKey(armName), new AlternativeEntry(armName));
    }

    return new Problem(analysis.getName(), alternatives);
  }

  private String createKey(String value) {
    //TODO: Expand properly
    return value.replace(" ", "-").replace("/", "");
  }
}

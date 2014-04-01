package org.drugis.addis.problems.service.impl;


import org.apache.commons.lang3.tuple.Pair;
import org.drugis.addis.analyses.Analysis;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.problems.model.*;
import org.drugis.addis.problems.service.AlternativeService;
import org.drugis.addis.problems.service.CriteriaService;
import org.drugis.addis.problems.service.MeasurementsService;
import org.drugis.addis.problems.service.ProblemService;
import org.drugis.addis.problems.service.model.AbstractMeasurementEntry;
import org.drugis.addis.projects.Project;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.util.JSONUtils;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
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
  ProjectRepository projectRepository;

  @Inject
  AlternativeService alternativeService;

  @Inject
  CriteriaService criteriaService;

  @Inject
  PerformanceTableBuilder performanceTableBuilder;

  @Inject
  JSONUtils jsonUtils;

  @Inject
  private MeasurementsService measurementsService;

  @Override
  public Problem getProblem(Integer projectId, Integer analysisId) throws ResourceDoesNotExistException {
    Project project = projectRepository.getProjectById(projectId);
    Analysis analysis = analysisRepository.get(projectId, analysisId);

    Map<Long, AlternativeEntry> alternativesCache = alternativeService.createAlternatives(project, analysis);
    Map<String, AlternativeEntry> alternatives = new HashMap<>();
    for(AlternativeEntry alternativeEntry : alternativesCache.values()) {
      alternatives.put(jsonUtils.createKey(alternativeEntry.getTitle()), alternativeEntry);
    }

    List<Pair<Variable, CriterionEntry>> variableCriteriaPairs = criteriaService.createVariableCriteriaPairs(project, analysis);

    Map<String, CriterionEntry> criteria = new HashMap<>();
    Map<Long, CriterionEntry> criteriaCache = new HashMap<>();
    for(Pair<Variable, CriterionEntry> variableCriterionPair : variableCriteriaPairs){
      Variable variable = variableCriterionPair.getLeft();
      CriterionEntry criterionEntry = variableCriterionPair.getRight();
      criteria.put(jsonUtils.createKey(variable.getName()), criterionEntry);
      criteriaCache.put(variable.getId(), criterionEntry);
    }

    List<Measurement> measurements = measurementsService.createMeasurements(project, analysis, alternativesCache);
    List<AbstractMeasurementEntry> performanceTable = performanceTableBuilder.build(criteriaCache, alternativesCache, measurements);
    return new Problem(analysis.getName(), alternatives, criteria, performanceTable);
  }




}

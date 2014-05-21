package org.drugis.addis.problems.service.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.tuple.Pair;
import org.drugis.addis.analyses.AbstractAnalysis;
import org.drugis.addis.analyses.NetworkMetaAnalysis;
import org.drugis.addis.analyses.SingleStudyBenefitRiskAnalysis;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.analyses.repository.SingleStudyBenefitRiskAnalysisRepository;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.Intervention;
import org.drugis.addis.interventions.repository.InterventionRepository;
import org.drugis.addis.problems.model.*;
import org.drugis.addis.problems.service.AlternativeService;
import org.drugis.addis.problems.service.CriteriaService;
import org.drugis.addis.problems.service.MeasurementsService;
import org.drugis.addis.problems.service.ProblemService;
import org.drugis.addis.problems.service.model.AbstractMeasurementEntry;
import org.drugis.addis.projects.Project;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.trialverse.service.TrialverseService;
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
  SingleStudyBenefitRiskAnalysisRepository singleStudyBenefitRiskAnalysisRepository;

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

  @Inject
  private AnalysisRepository analysisRepository;

  @Inject
  private TrialverseService trialverseService;

  @Inject
  private InterventionRepository interventionRepository;

  @Override
  public AbstractProblem getProblem(Integer projectId, Integer analysisId) throws ResourceDoesNotExistException {
    Project project = projectRepository.getProjectById(projectId);
    AbstractAnalysis analysis = analysisRepository.get(projectId, analysisId);
    if (analysis instanceof SingleStudyBenefitRiskAnalysis) {
      return getSingleStudyBenefitRiskProblem(project, (SingleStudyBenefitRiskAnalysis) analysis);
    } else if (analysis instanceof NetworkMetaAnalysis) {
      return getNetworkMetaAnalysisProblem(project, (NetworkMetaAnalysis) analysis);
    }
    throw new RuntimeException("unknown analysis type");
  }

  private NetworkMetaAnalysisProblem getNetworkMetaAnalysisProblem(Project project, NetworkMetaAnalysis analysis) {
    List<String> alternativeUris = new ArrayList<>();
    Collection<Intervention> interventions = interventionRepository.query(project.getId());
    Map<String, String> interventionNamesByUrisMap = new HashMap<>();
    for (Intervention intervention : interventions) {
      alternativeUris.add(intervention.getSemanticInterventionUri());
      interventionNamesByUrisMap.put(intervention.getSemanticInterventionUri(), intervention.getName());
    }
    ObjectNode trialData = trialverseService.getTrialData(Long.valueOf(project.getTrialverseId()),
      analysis.getOutcome().getSemanticOutcomeUri(), alternativeUris);
    ObjectMapper mapper = new ObjectMapper();
    TrialData convertedTrialData = mapper.convertValue(trialData, TrialData.class);
    Map<Long, TrialDataIntervention> interventionByDrugIdMap = createInterventionByDrugIdMap(convertedTrialData);

    List<AbstractNetworkMetaAnalysisProblemEntry> entries = new ArrayList<>();
    for (TrialDataStudy trialDataStudy : convertedTrialData.getTrialDataStudies()) {
      List<TrialDataArm> filteredArms = filterUnmatchedAndDuplicateArms(trialDataStudy, interventionByDrugIdMap);

      for (TrialDataArm trialDataArm : filteredArms) {
        String interventionUri = interventionByDrugIdMap.get(trialDataArm.getDrugId()).getUri();
        String treatmentName = interventionNamesByUrisMap.get(interventionUri);
        entries.add(buildEntry(trialDataStudy.getName(), treatmentName, trialDataArm.getMeasurements()));
      }

    }

    return new NetworkMetaAnalysisProblem(entries);
  }

  private AbstractNetworkMetaAnalysisProblemEntry buildEntry(String studyName, String treatmentName, List<Measurement> measurements) {
    Map<MeasurementAttribute, Measurement> measurementAttributeMeasurementMap = Measurement.mapMeasurementsByAttribute(measurements);
    Long sampleSize = measurementAttributeMeasurementMap.get(MeasurementAttribute.SAMPLE_SIZE).getIntegerValue();
    if (measurementAttributeMeasurementMap.get(MeasurementAttribute.MEAN) != null) {
      Double mu = measurementAttributeMeasurementMap.get(MeasurementAttribute.MEAN).getRealValue();
      Double sigma = measurementAttributeMeasurementMap.get(MeasurementAttribute.STANDARD_DEVIATION).getRealValue();
      return new ContinuousNetworkMetaAnalysisProblemEntry(studyName, treatmentName, sampleSize, mu, sigma);
    } else if (measurementAttributeMeasurementMap.get(MeasurementAttribute.RATE) != null) {
      Long rate = measurementAttributeMeasurementMap.get(MeasurementAttribute.RATE).getIntegerValue();
      return new RateNetworkMetaAnalysisProblemEntry(studyName, treatmentName, sampleSize, rate);
    }
    throw new RuntimeException("unknown measurement type");
  }

  private Map<Long, TrialDataIntervention> createInterventionByDrugIdMap(TrialData trialData) {
    Map<Long, TrialDataIntervention> interventionByDrugIdMap = new HashMap<>();
    for (TrialDataStudy study : trialData.getTrialDataStudies()) {

      for (TrialDataIntervention intervention : study.getTrialDataInterventions()) {
        interventionByDrugIdMap.put(intervention.getDrugId(), intervention);
      }
    }
    return interventionByDrugIdMap;
  }

  private List<TrialDataArm> filterUnmatchedAndDuplicateArms(TrialDataStudy study, Map<Long, TrialDataIntervention> interventionByIdMap) {
    List<TrialDataArm> arms = new ArrayList<>();

    for (TrialDataArm arm : study.getTrialDataArms()) {
      if (isMatched(arm, interventionByIdMap) && notDuplicateIntervention(arm, arms)) {
        arms.add(arm);
      }
    }
    return arms;
  }

  private boolean notDuplicateIntervention(TrialDataArm armToFind, List<TrialDataArm> arms) {
    for (TrialDataArm arm : arms) {
      if (arm.getDrugId().equals(armToFind.getDrugId())) {
        return false;
      }
    }
    return true;
  }

  private boolean isMatched(TrialDataArm arm, Map<Long, TrialDataIntervention> interventionByIdMap) {
    return interventionByIdMap.get(arm.getDrugId()) != null;
  }

  private SingleStudyBenefitRiskProblem getSingleStudyBenefitRiskProblem(Project project, SingleStudyBenefitRiskAnalysis analysis) throws ResourceDoesNotExistException {
    Map<Long, AlternativeEntry> alternativesCache = alternativeService.createAlternatives(project, analysis);
    Map<String, AlternativeEntry> alternatives = new HashMap<>();
    for (AlternativeEntry alternativeEntry : alternativesCache.values()) {
      alternatives.put(jsonUtils.createKey(alternativeEntry.getTitle()), alternativeEntry);
    }

    List<Pair<Variable, CriterionEntry>> variableCriteriaPairs = criteriaService.createVariableCriteriaPairs(project, analysis);

    Map<String, CriterionEntry> criteria = new HashMap<>();
    Map<Long, CriterionEntry> criteriaCache = new HashMap<>();
    for (Pair<Variable, CriterionEntry> variableCriterionPair : variableCriteriaPairs) {
      Variable variable = variableCriterionPair.getLeft();
      CriterionEntry criterionEntry = variableCriterionPair.getRight();
      criteria.put(jsonUtils.createKey(criterionEntry.getTitle()), criterionEntry);
      criteriaCache.put(variable.getId(), criterionEntry);
    }

    List<Measurement> measurements = measurementsService.createMeasurements(project, analysis, alternativesCache);
    List<AbstractMeasurementEntry> performanceTable = performanceTableBuilder.build(criteriaCache, alternativesCache, measurements);
    return new SingleStudyBenefitRiskProblem(analysis.getName(), alternatives, criteria, performanceTable);
  }


}

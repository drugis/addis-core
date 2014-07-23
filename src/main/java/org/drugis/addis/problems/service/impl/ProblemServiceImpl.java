package org.drugis.addis.problems.service.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.tuple.Pair;
import org.drugis.addis.analyses.*;
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
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;

/**
 * Created by daan on 3/21/14.
 */
@Service
public class ProblemServiceImpl implements ProblemService {

  @Inject
  private SingleStudyBenefitRiskAnalysisRepository singleStudyBenefitRiskAnalysisRepository;

  @Inject
  private ProjectRepository projectRepository;

  @Inject
  private AlternativeService alternativeService;

  @Inject
  private CriteriaService criteriaService;

  @Inject
  private PerformanceTableBuilder performanceTableBuilder;

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
    Project project = projectRepository.get(projectId);
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
    List<Intervention> interventions = interventionRepository.query(project.getId());
    Map<String, Integer> interventionIdsByUrisMap = new HashMap<>();

    interventions = filterExcludedInterventions(interventions, analysis.getIncludedInterventions());

    List<TreatmentEntry> treatments = new ArrayList<>();
    for (Intervention intervention : interventions) {
      alternativeUris.add(intervention.getSemanticInterventionUri());
      interventionIdsByUrisMap.put(intervention.getSemanticInterventionUri(), intervention.getId());
      treatments.add(new TreatmentEntry(intervention.getId(), intervention.getName()));
    }

    ObjectNode trialData = trialverseService.getTrialData(project.getNamespaceUid(),
            analysis.getOutcome().getSemanticOutcomeUri(), alternativeUris);

    ObjectMapper mapper = new ObjectMapper();
    TrialData convertedTrialData = mapper.convertValue(trialData, TrialData.class);
    Map<String, TrialDataIntervention> interventionByDrugIdMap = createInterventionByDrugIdMap(convertedTrialData);

    List<AbstractNetworkMetaAnalysisProblemEntry> entries = new ArrayList<>();

    for (TrialDataStudy trialDataStudy : convertedTrialData.getTrialDataStudies()) {
      List<TrialDataArm> filteredArms = filterUnmatchedArms(trialDataStudy, interventionByDrugIdMap);
      filteredArms = filterExcludedArms(filteredArms, analysis);

      // do not include studies with fewer than two included and matched arms
      if (filteredArms.size() >= 2) {

        for (TrialDataArm trialDataArm : filteredArms) {
          String interventionUri = interventionByDrugIdMap.get(trialDataArm.getDrugUid()).getUri();
          Integer treatmentId = interventionIdsByUrisMap.get(interventionUri);
          entries.add(buildEntry(trialDataStudy.getName(), treatmentId, trialDataArm.getMeasurement()));
        }

      }
    }

    return new NetworkMetaAnalysisProblem(entries, treatments);
  }

  private List<TrialDataArm> filterExcludedArms(List<TrialDataArm> trialDataArms, NetworkMetaAnalysis analysis) {
    List<TrialDataArm> filteredTrialDataArms = new ArrayList<>();
    List<ArmExclusion> armExclusions = analysis.getExcludedArms();
    List<Long> armExclusionTrialverseIds = new ArrayList<>(armExclusions.size());

    for (ArmExclusion armExclusion : armExclusions) {
      armExclusionTrialverseIds.add(armExclusion.getTrialverseId());
    }

    for (TrialDataArm trialDataArm : trialDataArms) {
      if (!armExclusionTrialverseIds.contains(trialDataArm.getUid())) {
        filteredTrialDataArms.add(trialDataArm);
      }
    }

    return filteredTrialDataArms;
  }

  private AbstractNetworkMetaAnalysisProblemEntry buildEntry(String studyName, Integer treatmentId, Measurement measurement) {
    Long sampleSize = measurement.getSampleSize();
    if (measurement.getMean() != null) {
      Double mu = measurement.getMean();
      Double sigma = measurement.getStdDev();
      return new ContinuousNetworkMetaAnalysisProblemEntry(studyName, treatmentId, sampleSize, mu, sigma);
    } else if (measurement.getRate() != null) {
      Long rate = measurement.getRate();
      return new RateNetworkMetaAnalysisProblemEntry(studyName, treatmentId, sampleSize, rate);
    }
    throw new RuntimeException("unknown measurement type");
  }

  private Map<String, TrialDataIntervention> createInterventionByDrugIdMap(TrialData trialData) {
    Map<String, TrialDataIntervention> interventionByDrugIdMap = new HashMap<>();
    for (TrialDataStudy study : trialData.getTrialDataStudies()) {

      for (TrialDataIntervention intervention : study.getTrialDataInterventions()) {
        interventionByDrugIdMap.put(intervention.getDrugUid(), intervention);
      }
    }
    return interventionByDrugIdMap;
  }

  private List<Intervention> filterExcludedInterventions(List<Intervention> interventions, List<InterventionInclusion> inclusions) {
    List<Intervention> filteredInterventions = new ArrayList<>();

    Map<Integer, InterventionInclusion> inclusionMap = new HashMap<>(inclusions.size());
    for (InterventionInclusion interventionInclusion : inclusions) {
      inclusionMap.put(interventionInclusion.getInterventionId(), interventionInclusion);
    }

    for (Intervention intervention : interventions) {
      if (inclusionMap.get(intervention.getId()) != null) {
        filteredInterventions.add(intervention);
      }
    }

    return filteredInterventions;
  }

  private List<TrialDataArm> filterUnmatchedArms(TrialDataStudy study, Map<String, TrialDataIntervention> interventionByIdMap) {
    List<TrialDataArm> filteredArms = new ArrayList<>();

    List<TrialDataArm> studyArmsSortedByName = sortTrialDataArmsByName(study.getTrialDataArms());

    for (TrialDataArm arm : studyArmsSortedByName) {
      if (isMatched(arm, interventionByIdMap)) {
        filteredArms.add(arm);
      }
    }

    return filteredArms;
  }

  private List<TrialDataArm> sortTrialDataArmsByName(List<TrialDataArm> trialDataArms) {
    Collections.sort(trialDataArms, new Comparator<TrialDataArm>() {
      @Override
      public int compare(TrialDataArm leftTrialDataArm, TrialDataArm rightTrialDataArm) {
        return leftTrialDataArm.getName().compareTo(rightTrialDataArm.getName());
      }
    });
    return trialDataArms;
  }

  private boolean isMatched(TrialDataArm arm, Map<String, TrialDataIntervention> interventionByIdMap) {
    return interventionByIdMap.get(arm.getDrugUid()) != null;
  }

  private SingleStudyBenefitRiskProblem getSingleStudyBenefitRiskProblem(Project project, SingleStudyBenefitRiskAnalysis analysis) throws ResourceDoesNotExistException {
    Map<String, AlternativeEntry> alternativesCache = alternativeService.createAlternatives(project, analysis);
    Map<String, AlternativeEntry> alternatives = new HashMap<>();
    for (AlternativeEntry alternativeEntry : alternativesCache.values()) {
      alternatives.put(alternativeEntry.getAlternativeUri(), alternativeEntry);
    }

    List<Pair<Variable, CriterionEntry>> variableCriteriaPairs = criteriaService.createVariableCriteriaPairs(project, analysis);

    Map<String, CriterionEntry> criteria = new HashMap<>();
    Map<String, CriterionEntry> criteriaCache = new HashMap<>();
    for (Pair<Variable, CriterionEntry> variableCriterionPair : variableCriteriaPairs) {
      Variable variable = variableCriterionPair.getLeft();
      CriterionEntry criterionEntry = variableCriterionPair.getRight();
      criteria.put(criterionEntry.getCriterionUri(), criterionEntry);
      criteriaCache.put(variable.getUid(), criterionEntry);
    }

    List<Measurement> measurements = measurementsService.createMeasurements(project, analysis, alternativesCache);
    List<AbstractMeasurementEntry> performanceTable = performanceTableBuilder.build(criteriaCache, alternativesCache, measurements);
    return new SingleStudyBenefitRiskProblem(analysis.getName(), alternatives, criteria, performanceTable);
  }


}

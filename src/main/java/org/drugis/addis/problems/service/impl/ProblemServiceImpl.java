package org.drugis.addis.problems.service.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.drugis.addis.analyses.*;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.analyses.repository.SingleStudyBenefitRiskAnalysisRepository;
import org.drugis.addis.covariates.Covariate;
import org.drugis.addis.covariates.CovariateRepository;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.Intervention;
import org.drugis.addis.interventions.repository.InterventionRepository;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.problems.model.*;
import org.drugis.addis.problems.service.ProblemService;
import org.drugis.addis.problems.service.model.AbstractMeasurementEntry;
import org.drugis.addis.projects.Project;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.trialverse.service.TrialverseService;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.drugis.addis.trialverse.service.impl.TriplestoreServiceImpl;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
  private PerformanceTableBuilder performanceTableBuilder;

  @Inject
  private AnalysisRepository analysisRepository;

  @Inject
  private TrialverseService trialverseService;

  @Inject
  private InterventionRepository interventionRepository;

  @Inject
  private TriplestoreService triplestoreService;

  @Inject
  private CovariateRepository covariateRepository;

  @Override
  public AbstractProblem getProblem(Integer projectId, Integer analysisId) throws ResourceDoesNotExistException {
    Project project = projectRepository.get(projectId);
    AbstractAnalysis analysis = analysisRepository.get(analysisId);
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

    Map<Integer, Covariate> definedMap = covariateRepository
            .findByProject(project.getId())
            .stream()
            .collect(Collectors.toMap(Covariate::getId, Function.identity()));
    List<String> includedCovariateKeys = analysis.getCovariateInclusions().stream()
            .map(ic -> definedMap.get(ic.getId()).getDefinitionKey())
            .collect(Collectors.toList());

    List<ObjectNode> trialDataStudies = trialverseService.getTrialData(project.getNamespaceUid(), project.getDatasetVersion(),
            analysis.getOutcome().getSemanticOutcomeUri(), alternativeUris, includedCovariateKeys);
    ObjectMapper mapper = new ObjectMapper();
    List<TrialDataStudy> convertedTrialDataStudies = new ArrayList<>();
    for (ObjectNode objectNode : trialDataStudies) {
      convertedTrialDataStudies.add(mapper.convertValue(objectNode, TrialDataStudy.class));
    }
    Map<String, TrialDataIntervention> interventionByDrugUidInstanceMap = createInterventionByDrugIdMap(convertedTrialDataStudies);

    List<AbstractNetworkMetaAnalysisProblemEntry> entries = new ArrayList<>();

    for (TrialDataStudy trialDataStudy : convertedTrialDataStudies) {
      List<TrialDataArm> filteredArms = filterUnmatchedArms(trialDataStudy, interventionIdsByUrisMap);
      filteredArms = filterExcludedArms(filteredArms, analysis);

      // do not include studies with fewer than two included and matched arms
      if (filteredArms.size() >= 2) {

        for (TrialDataArm trialDataArm : filteredArms) {
          Integer treatmentId = interventionIdsByUrisMap.get(trialDataArm.getDrugConceptUid());
          entries.add(buildEntry(trialDataStudy.getName(), treatmentId, trialDataArm.getMeasurement()));
        }

      }
    }

    return new NetworkMetaAnalysisProblem(entries, treatments);
  }

  private List<TrialDataArm> filterExcludedArms(List<TrialDataArm> trialDataArms, NetworkMetaAnalysis analysis) {
    List<TrialDataArm> filteredTrialDataArms = new ArrayList<>();
    List<ArmExclusion> armExclusions = analysis.getExcludedArms();
    List<String> armExclusionTrialverseIds = new ArrayList<>(armExclusions.size());

    for (ArmExclusion armExclusion : armExclusions) {
      armExclusionTrialverseIds.add(armExclusion.getTrialverseUid());
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

  private Map<String, TrialDataIntervention> createInterventionByDrugIdMap(List<TrialDataStudy> trialDataStudies) {
    Map<String, TrialDataIntervention> interventionByDrugIdMap = new HashMap<>();
    for (TrialDataStudy study : trialDataStudies) {

      for (TrialDataIntervention intervention : study.getTrialDataInterventions()) {
        interventionByDrugIdMap.put(intervention.getDrugInstanceUid(), intervention);
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

  private List<TrialDataArm> filterUnmatchedArms(TrialDataStudy study, Map<String, Integer> interventionByIdMap) {
    List<TrialDataArm> filteredArms = new ArrayList<>();

    for (TrialDataArm arm : study.getTrialDataArms()) {
      if (isMatched(arm, interventionByIdMap)) {
        filteredArms.add(arm);
      }
    }

    return filteredArms;
  }

  private boolean isMatched(TrialDataArm arm, Map<String, Integer> interventionByIdMap) {
    return interventionByIdMap.get(arm.getDrugConceptUid()) != null;
  }

  private SingleStudyBenefitRiskProblem getSingleStudyBenefitRiskProblem(Project project, SingleStudyBenefitRiskAnalysis analysis) throws ResourceDoesNotExistException {
    List<String> outcomeUids = new ArrayList<>();
    for (Outcome outcome : analysis.getSelectedOutcomes()) {
      outcomeUids.add(outcome.getSemanticOutcomeUri());
    }
    List<String> alternativeUids = new ArrayList<>();
    for (Intervention intervention : analysis.getSelectedInterventions()) {
      alternativeUids.add(intervention.getSemanticInterventionUri());
    }
    List<TriplestoreServiceImpl.SingleStudyBenefitRiskMeasurementRow> measurementNodes =
            triplestoreService.getSingleStudyMeasurements(project.getNamespaceUid(), analysis.getStudyGraphUid(), project.getDatasetVersion(), outcomeUids, alternativeUids);

    Map<String, AlternativeEntry> alternatives = new HashMap<>();
    Map<String, CriterionEntry> criteria = new HashMap<>();
    for (TriplestoreServiceImpl.SingleStudyBenefitRiskMeasurementRow measurementRow : measurementNodes) {
      alternatives.put(measurementRow.getAlternativeUid(), new AlternativeEntry(measurementRow.getAlternativeUid(), measurementRow.getAlternativeLabel()));
      CriterionEntry criterionEntry = createCriterionEntry(measurementRow);
      criteria.put(measurementRow.getOutcomeUid(), criterionEntry);
    }

    List<AbstractMeasurementEntry> performanceTable = performanceTableBuilder.build(measurementNodes);
    return new SingleStudyBenefitRiskProblem(analysis.getTitle(), alternatives, criteria, performanceTable);
  }

  private CriterionEntry createCriterionEntry(TriplestoreServiceImpl.SingleStudyBenefitRiskMeasurementRow measurementRow) throws EnumConstantNotPresentException {
    List<Double> scale;
    if (measurementRow.getRate() != null) { // rate measurement
      scale = Arrays.asList(0.0, 1.0);
    } else if (measurementRow.getMean() != null) { // continuous measurement
      scale = Arrays.asList(null, null);
    } else {
      throw new RuntimeException("Invalid measurement");
    }
    // NB: partialvaluefunctions to be filled in by MCDA component, left null here
    return new CriterionEntry(measurementRow.getOutcomeUid(), measurementRow.getOutcomeLabel(), scale, null);
  }


}

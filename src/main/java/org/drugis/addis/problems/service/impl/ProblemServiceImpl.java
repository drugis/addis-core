package org.drugis.addis.problems.service.impl;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.drugis.addis.analyses.*;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.analyses.repository.SingleStudyBenefitRiskAnalysisRepository;
import org.drugis.addis.covariates.Covariate;
import org.drugis.addis.covariates.CovariateRepository;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.Intervention;
import org.drugis.addis.interventions.repository.InterventionRepository;
import org.drugis.addis.models.Model;
import org.drugis.addis.models.repository.ModelRepository;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.outcomes.repository.OutcomeRepository;
import org.drugis.addis.patavitask.PataviTask;
import org.drugis.addis.patavitask.repository.PataviTaskRepository;
import org.drugis.addis.problems.model.*;
import org.drugis.addis.problems.service.ProblemService;
import org.drugis.addis.problems.service.model.AbstractMeasurementEntry;
import org.drugis.addis.projects.Project;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.trialverse.service.MappingService;
import org.drugis.addis.trialverse.service.TrialverseService;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.drugis.addis.trialverse.service.impl.TriplestoreServiceImpl;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.function.ToIntFunction;
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

  @Inject
  private MappingService mappingService;

  @Inject
  private ModelRepository modelRepository;

  @Inject
  private OutcomeRepository outcomeRepository;

  @Inject
  private PataviTaskRepository pataviTaskRepository;

  private ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public AbstractProblem getProblem(Integer projectId, Integer analysisId) throws ResourceDoesNotExistException, URISyntaxException, SQLException, IOException {
    Project project = projectRepository.get(projectId);
    AbstractAnalysis analysis = analysisRepository.get(analysisId);
    if (analysis instanceof SingleStudyBenefitRiskAnalysis) {
      return getSingleStudyBenefitRiskProblem(project, (SingleStudyBenefitRiskAnalysis) analysis);
    } else if (analysis instanceof NetworkMetaAnalysis) {
      return getNetworkMetaAnalysisProblem(project, (NetworkMetaAnalysis) analysis);
    } else if (analysis instanceof MetaBenefitRiskAnalysis) {
      return getMetaBenefitRiskAnalysisProblem(project, (MetaBenefitRiskAnalysis) analysis);
    }
    throw new RuntimeException("unknown analysis type");
  }

  private MetaBenefitRiskProblem getMetaBenefitRiskAnalysisProblem(Project project, MetaBenefitRiskAnalysis analysis) throws SQLException, IOException {
    final List<Integer> modelIdsWithBaseline = getInclusionIdsWithBaseline(analysis.getMbrOutcomeInclusions(), MbrOutcomeInclusion::getModelId);
    final List<Integer> outcomeIdsWithBaseline = getInclusionIdsWithBaseline(analysis.getMbrOutcomeInclusions(), MbrOutcomeInclusion::getOutcomeId);
    final List<Model> models = modelRepository.get(modelIdsWithBaseline);
    final Map<Integer, Model> modelMap = models.stream().collect(Collectors.toMap(Model::getId, Function.identity()));
    List<Outcome> outcomes = outcomeRepository.get(project.getId(), outcomeIdsWithBaseline);
    final Map<String, Outcome> outcomesByName = outcomes.stream().collect(Collectors.toMap(Outcome::getName, Function.identity()));
    final Map<Integer, Outcome> outcomesById = outcomes.stream().collect(Collectors.toMap(Outcome::getId, Function.identity()));
    final Map<Integer, PataviTask> pataviTaskMap = pataviTaskRepository.findByIds(models.stream().map(Model::getTaskId).collect(Collectors.toList()))
            .stream().collect(Collectors.toMap(PataviTask::getId, Function.identity()));
    final Map<Integer, PataviTask> tasksByModelId = models.stream().collect(Collectors.toMap(Model::getId, m -> pataviTaskMap.get(m.getTaskId())));
    ArrayList<Integer> taskIds = new ArrayList<>(pataviTaskMap.keySet());
    final Map<Integer, JsonNode> resultsByTaskId = pataviTaskRepository.getResults(taskIds);
    List<MbrOutcomeInclusion> inclusionsWithBaseline = analysis.getMbrOutcomeInclusions().stream().filter(moi -> moi.getBaseline() != null).collect(Collectors.toList());

    List<Intervention> includedAlternatives = analysis.getIncludedAlternatives();

    Map<String, CriterionEntry> criteriaWithBaseline = outcomesByName.values()
            .stream()
            .collect(Collectors.toMap(Outcome::getName, o -> new CriterionEntry(o.getSemanticOutcomeUri(), o.getName())));
    Map<String, AlternativeEntry> alternatives = includedAlternatives
            .stream()
            .collect(Collectors.toMap(Intervention::getName, i -> new AlternativeEntry(i.getSemanticInterventionUri(), i.getName())));
    final Map<Integer, Intervention> interventions = interventionRepository.query(project.getId()).stream().collect(Collectors.toMap(Intervention::getId, Function.identity()));
    ;
    final Map<String, Intervention> includedInterventionsByName = includedAlternatives.stream().collect(Collectors.toMap(Intervention::getName, Function.identity()));

    List<MetaBenefitRiskProblem.PerformanceTableEntry> performanceTable = new ArrayList<>(outcomesByName.size());
    for (MbrOutcomeInclusion outcomeInclusion : inclusionsWithBaseline) {

      Baseline baseline = objectMapper.readValue(outcomeInclusion.getBaseline(), Baseline.class);
      Integer taskId = tasksByModelId.get(outcomeInclusion.getModelId()).getId();
      JsonNode taskResults = resultsByTaskId.get(taskId);

      Map<Integer, MultiVariateDistribution> distributionByInterventionId = objectMapper
              .readValue(taskResults.get("multivariateSummary").toString(), new TypeReference<Map<Integer, MultiVariateDistribution>>() {
              });

      Intervention baselineIntervention = includedInterventionsByName.get(baseline.getName());
      MultiVariateDistribution distr = distributionByInterventionId.get(baselineIntervention.getId());
      //
      Map<String, Double> mu = distr.getMu().entrySet().stream()
              .collect(Collectors.toMap(
                      e -> {
                        String key = e.getKey();
                        int interventionId = Integer.parseInt(key.substring(key.lastIndexOf('.') + 1));
                        return interventions.get(interventionId).getName();
                      },
                      Map.Entry::getValue));

      // filter mu
      mu = mu.entrySet().stream().filter(m -> includedInterventionsByName.keySet().contains(m.getKey()))
              .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

      //add baseline to mu
      mu.put(baseline.getName(), 0.0);


      List<String> rowNames = new ArrayList<>();
      rowNames.addAll(mu.keySet());

      // place baseline at the front of the list
      rowNames.sort((rn1, rn2) -> rn1.equals(baseline.getName()) ? -1 : 0);


      final List<String> colNames = rowNames;

      Map<Pair<String, String>, Double> dataMap = new HashMap<>();

      final Map<String, Map<String, Double>> sigma = distr.getSigma();
      for (String interventionY : rowNames) {
        for (String interventionX : rowNames) {
          if (!interventionX.equals(baseline.getName()) && !interventionY.equals(baseline.getName())) {
            Double value =
                    sigma
                            .get(getD(includedInterventionsByName, baseline.getName(), interventionX))
                            .get(getD(includedInterventionsByName, baseline.getName(), interventionY));
            dataMap.put(new ImmutablePair<>(interventionX, interventionY), value);
            dataMap.put(new ImmutablePair<>(interventionY, interventionX), value);
          }
        }
      }

      final List<List<Double>> data = new ArrayList<>(rowNames.size());

      // setup data structure and init with null values
      for (int i = 0; i < rowNames.size(); ++i) {
        List<Double> row = new ArrayList<>(colNames.size());
        for (int j = 0; j < colNames.size(); ++j) {
          row.add(0.0);
        }
        data.add(row);
      }


      for (String rowName : rowNames) {
        for (String colName : colNames) {
          if (!baseline.getName().equals(rowName) && !baseline.getName().equals(colName)) {
            data.get(rowNames.indexOf(rowName)).set(colNames.indexOf(colName), dataMap.get(ImmutablePair.of(rowName, colName)));
          }
        }
      }

      MetaBenefitRiskProblem.PerformanceTableEntry.Performance.Parameters.Relative.CovarianceMatrix cov =
              new MetaBenefitRiskProblem.PerformanceTableEntry.Performance.Parameters.Relative.CovarianceMatrix(rowNames, colNames, data);
      MetaBenefitRiskProblem.PerformanceTableEntry.Performance.Parameters.Relative relative =
              new MetaBenefitRiskProblem.PerformanceTableEntry.Performance.Parameters.Relative("dmnorm", mu, cov);
      MetaBenefitRiskProblem.PerformanceTableEntry.Performance.Parameters parameters =
              new MetaBenefitRiskProblem.PerformanceTableEntry.Performance.Parameters(outcomeInclusion.getBaseline(), relative);
      String modelLinkType = modelMap.get(outcomeInclusion.getModelId()).getLink();

      String modelPerformanceType = "relative-normal";
      if (!Model.LINK_IDENTITY.equals(modelLinkType)) {
        modelPerformanceType = "relative-" + modelLinkType + "-normal";
      }

      MetaBenefitRiskProblem.PerformanceTableEntry.Performance performance =
              new MetaBenefitRiskProblem.PerformanceTableEntry.Performance(modelPerformanceType, parameters);

      MetaBenefitRiskProblem.PerformanceTableEntry entry =
              new MetaBenefitRiskProblem.PerformanceTableEntry(outcomesById.get(outcomeInclusion.getOutcomeId()).getName(), performance);
      performanceTable.add(entry);
    }

    return new MetaBenefitRiskProblem(criteriaWithBaseline, alternatives, performanceTable);
  }

  private String getD(Map<String, Intervention> includedInterventionsByName, String base, String otherIntervention) {
    return "d." + includedInterventionsByName.get(base).getId() + '.' + includedInterventionsByName.get(otherIntervention).getId();
  }

  private List<Integer> getInclusionIdsWithBaseline(List<MbrOutcomeInclusion> outcomeInclusions, ToIntFunction<MbrOutcomeInclusion> idSelector) {
    return outcomeInclusions.stream()
            .filter(moi -> moi.getBaseline() != null)
            .mapToInt(idSelector).boxed().collect(Collectors.toList());
  }

  private NetworkMetaAnalysisProblem getNetworkMetaAnalysisProblem(Project project, NetworkMetaAnalysis analysis) throws URISyntaxException {
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

    Collection<Covariate> projectCovariates = covariateRepository.findByProject(project.getId());
    Map<Integer, Covariate> definedMap = projectCovariates
            .stream()
            .collect(Collectors.toMap(Covariate::getId, Function.identity()));
    List<String> includedCovariateKeys = analysis.getCovariateInclusions().stream()
            .map(ic -> definedMap.get(ic.getCovariateId()).getDefinitionKey())
            .collect(Collectors.toList());

    String namespaceUid = mappingService.getVersionedUuid(project.getNamespaceUid());
    List<ObjectNode> trialDataStudies = trialverseService.getTrialData(namespaceUid, project.getDatasetVersion(),
            analysis.getOutcome().getSemanticOutcomeUri(), alternativeUris, includedCovariateKeys);
    ObjectMapper mapper = new ObjectMapper();
    List<TrialDataStudy> convertedTrialDataStudies = new ArrayList<>();
    for (ObjectNode objectNode : trialDataStudies) {
      convertedTrialDataStudies.add(mapper.convertValue(objectNode, TrialDataStudy.class));
    }

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

    // add covariate values to problem
    Map<String, Map<String, Double>> studyLevelCovariates = null;
    if (includedCovariateKeys.size() > 0) {
      studyLevelCovariates = new HashMap<>(convertedTrialDataStudies.size());
      Map<String, Covariate> covariatesByKey = projectCovariates
              .stream()
              .collect(Collectors.toMap(Covariate::getDefinitionKey, Function.identity()));
      for (TrialDataStudy trialDataStudy : convertedTrialDataStudies) {
        Map<String, Double> covariateNodes = new HashMap<>();
        for (CovariateStudyValue covariateStudyValue : trialDataStudy.getCovariateValues()) {
          Covariate covariate = covariatesByKey.get(covariateStudyValue.getCovariateKey());
          covariateNodes.put(covariate.getName(), covariateStudyValue.getValue());
        }
        studyLevelCovariates.put(trialDataStudy.getName(), covariateNodes);
      }
    }

    return new NetworkMetaAnalysisProblem(entries, treatments, studyLevelCovariates);
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

  private SingleStudyBenefitRiskProblem getSingleStudyBenefitRiskProblem(Project project, SingleStudyBenefitRiskAnalysis analysis) throws ResourceDoesNotExistException, URISyntaxException {
    List<String> outcomeUids = new ArrayList<>();
    for (Outcome outcome : analysis.getSelectedOutcomes()) {
      outcomeUids.add(outcome.getSemanticOutcomeUri());
    }
    List<String> alternativeUids = new ArrayList<>();
    for (Intervention intervention : analysis.getSelectedInterventions()) {
      alternativeUids.add(intervention.getSemanticInterventionUri());
    }
    String versionedUuid = mappingService.getVersionedUuid(project.getNamespaceUid());
    List<TriplestoreServiceImpl.SingleStudyBenefitRiskMeasurementRow> measurementNodes =
            triplestoreService.getSingleStudyMeasurements(versionedUuid, analysis.getStudyGraphUid(), project.getDatasetVersion(), outcomeUids, alternativeUids);

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

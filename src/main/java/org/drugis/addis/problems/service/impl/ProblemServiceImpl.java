package org.drugis.addis.problems.service.impl;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.ext.com.google.common.collect.ImmutableSet;
import org.drugis.addis.analyses.*;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.analyses.repository.SingleStudyBenefitRiskAnalysisRepository;
import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.covariates.Covariate;
import org.drugis.addis.covariates.CovariateRepository;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.interventions.repository.InterventionRepository;
import org.drugis.addis.interventions.service.InterventionService;
import org.drugis.addis.interventions.service.impl.InvalidTypeForDoseCheckException;
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
import org.drugis.addis.trialverse.model.trialdata.CovariateStudyValue;
import org.drugis.addis.trialverse.model.trialdata.Measurement;
import org.drugis.addis.trialverse.model.trialdata.TrialDataArm;
import org.drugis.addis.trialverse.model.trialdata.TrialDataStudy;
import org.drugis.addis.trialverse.service.MappingService;
import org.drugis.addis.trialverse.service.TrialverseService;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.drugis.addis.trialverse.service.impl.ReadValueException;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
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

  @Inject
  private AnalysisService analysisService;

  @Inject
  private InterventionService interventionService;

  private ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public AbstractProblem getProblem(Integer projectId, Integer analysisId) throws ResourceDoesNotExistException, URISyntaxException, SQLException, IOException, ReadValueException, InvalidTypeForDoseCheckException {
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
    final List<Integer> networkModelIds = getInclusionIdsWithBaseline(analysis.getMbrOutcomeInclusions(), MbrOutcomeInclusion::getModelId);
    final List<Integer> outcomeIds = getInclusionIdsWithBaseline(analysis.getMbrOutcomeInclusions(), MbrOutcomeInclusion::getOutcomeId);
    final List<Model> models = modelRepository.get(networkModelIds);
    final Map<Integer, Model> modelMap = models.stream().collect(Collectors.toMap(Model::getId, Function.identity()));
    List<Outcome> outcomes = outcomeRepository.get(project.getId(), outcomeIds);
    final Map<String, Outcome> outcomesByName = outcomes.stream().collect(Collectors.toMap(Outcome::getName, Function.identity()));
    final Map<Integer, Outcome> outcomesById = outcomes.stream().collect(Collectors.toMap(Outcome::getId, Function.identity()));
    final Map<Integer, PataviTask> pataviTaskMap = pataviTaskRepository.findByIds(models.stream().map(Model::getTaskId).collect(Collectors.toList()))
            .stream().collect(Collectors.toMap(PataviTask::getId, Function.identity()));
    final Map<Integer, PataviTask> tasksByModelId = models.stream().collect(Collectors.toMap(Model::getId, m -> pataviTaskMap.get(m.getTaskId())));
    ArrayList<Integer> taskIds = new ArrayList<>(pataviTaskMap.keySet());
    final Map<Integer, JsonNode> resultsByTaskId = pataviTaskRepository.getResults(taskIds);
    final List<MbrOutcomeInclusion> inclusionsWithBaseline = analysis.getMbrOutcomeInclusions().stream().filter(moi -> moi.getBaseline() != null).collect(Collectors.toList());
    final List<InterventionInclusion> inclusions = analysis.getInterventionInclusions();
    final List<AbstractIntervention> interventions = interventionRepository.query(analysis.getProjectId());
    final Map<Integer, AbstractIntervention> interventionMap = interventions.stream()
            .collect(Collectors.toMap(AbstractIntervention::getId, Function.identity()));
    final List<AbstractIntervention> includedAlternatives = inclusions
            .stream()
            .map(i -> interventionMap.get(i.getInterventionId()))
            .collect(Collectors.toList());

    Map<String, CriterionEntry> criteriaWithBaseline = outcomesByName.values()
            .stream()
            .filter(o -> inclusionsWithBaseline.stream().filter(moi -> moi.getOutcomeId().equals(o.getId())).findFirst().isPresent())
            .collect(Collectors.toMap(Outcome::getName, o -> new CriterionEntry(o.getSemanticOutcomeUri(), o.getName())));
    Map<String, AlternativeEntry> alternatives = includedAlternatives
            .stream()
            .collect(Collectors.toMap(AbstractIntervention::getName, i -> new AlternativeEntry(i.getId(), i.getName())));

    final Map<String, AbstractIntervention> includedInterventionsByName = includedAlternatives.stream().collect(Collectors.toMap(AbstractIntervention::getName, Function.identity()));

    List<MetaBenefitRiskProblem.PerformanceTableEntry> performanceTable = new ArrayList<>(outcomesByName.size());
    for (MbrOutcomeInclusion outcomeInclusion : inclusionsWithBaseline) {

      Baseline baseline = objectMapper.readValue(outcomeInclusion.getBaseline(), Baseline.class);
      Integer taskId = tasksByModelId.get(outcomeInclusion.getModelId()).getId();
      JsonNode taskResults = resultsByTaskId.get(taskId);

      Map<Integer, MultiVariateDistribution> distributionByInterventionId = objectMapper
              .readValue(taskResults.get("multivariateSummary").toString(), new TypeReference<Map<Integer, MultiVariateDistribution>>() {
              });

      AbstractIntervention baselineIntervention = includedInterventionsByName.get(baseline.getName());
      MultiVariateDistribution distr = distributionByInterventionId.get(baselineIntervention.getId());
      //
      Map<String, Double> mu = distr.getMu().entrySet().stream()
              .collect(Collectors.toMap(
                      e -> {
                        String key = e.getKey();
                        int interventionId = Integer.parseInt(key.substring(key.lastIndexOf('.') + 1));
                        return interventionMap.get(interventionId).getName();
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

  private String getD(Map<String, AbstractIntervention> includedInterventionsByName, String base, String otherIntervention) {
    return "d." + includedInterventionsByName.get(base).getId() + '.' + includedInterventionsByName.get(otherIntervention).getId();
  }

  private List<Integer> getInclusionIdsWithBaseline(List<MbrOutcomeInclusion> outcomeInclusions, ToIntFunction<MbrOutcomeInclusion> idSelector) {
    return outcomeInclusions.stream()

            .mapToInt(idSelector).boxed().collect(Collectors.toList());
  }

  private NetworkMetaAnalysisProblem getNetworkMetaAnalysisProblem(Project project, NetworkMetaAnalysis analysis) throws URISyntaxException, ReadValueException, ResourceDoesNotExistException {

    List<AbstractIntervention> interventions = interventionRepository.query(project.getId());

    interventions = filterExcludedInterventions(interventions, analysis.getInterventionInclusions());

    List<TreatmentEntry> treatments = interventions.stream()
            .map(intervention -> new TreatmentEntry(intervention.getId(), intervention.getName()))
            .collect(Collectors.toList());

    Collection<Covariate> projectCovariates = covariateRepository.findByProject(project.getId());
    Map<Integer, Covariate> definedMap = projectCovariates
            .stream()
            .collect(Collectors.toMap(Covariate::getId, Function.identity()));
    List<String> includedCovariateKeys = analysis.getCovariateInclusions().stream()
            .map(ic -> definedMap.get(ic.getCovariateId()).getDefinitionKey()).sorted()
            .collect(Collectors.toList());

    List<TrialDataStudy> trialDataStudies = analysisService.buildEvidenceTable(project.getId(), analysis.getId());

    List<AbstractNetworkMetaAnalysisProblemEntry> entries = new ArrayList<>();

    for (TrialDataStudy trialDataStudy : trialDataStudies) {
      List<TrialDataArm> filteredArms = filterUnmatchedArms(trialDataStudy);
      filteredArms = filterExcludedArms(filteredArms, analysis);

      // do not include studies with fewer than two included and matched arms
      if (filteredArms.size() >= 2) {
        entries.addAll(filteredArms.stream()
                .map(trialDataArm -> buildEntry(trialDataStudy.getName(),
                        trialDataArm.getMatchedProjectInterventionIds().iterator().next(), // safe because we filter unmatched arms
                        trialDataArm.getMeasurements().get(0)))  // nma has exactly one measurement
                .collect(Collectors.toList()));
      }
    }

    // add covariate values to problem
    Map<String, Map<String, Double>> studyLevelCovariates = null;
    if (includedCovariateKeys.size() > 0) {
      studyLevelCovariates = new HashMap<>(trialDataStudies.size());
      Map<String, Covariate> covariatesByKey = projectCovariates
              .stream()
              .collect(Collectors.toMap(Covariate::getDefinitionKey, Function.identity()));
      for (TrialDataStudy trialDataStudy : trialDataStudies) {
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

  private List<TrialDataArm> filterUnmatchedArms(TrialDataStudy trialDataStudy) {
    return trialDataStudy.getTrialDataArms()
            .stream()
            .filter(a -> a.getMatchedProjectInterventionIds().size() > 0)
            .collect(Collectors.toList());
  }

  private List<TrialDataArm> filterExcludedArms(List<TrialDataArm> trialDataArms, NetworkMetaAnalysis analysis) {
    List<TrialDataArm> filteredTrialDataArms = new ArrayList<>();
    List<ArmExclusion> armExclusions = analysis.getExcludedArms();
    List<URI> armExclusionTrialverseIds = new ArrayList<>(armExclusions.size());

    for (ArmExclusion armExclusion : armExclusions) {
      armExclusionTrialverseIds.add(armExclusion.getTrialverseUid());
    }

    for (TrialDataArm trialDataArm : trialDataArms) {
      if (!armExclusionTrialverseIds.contains(trialDataArm.getUri())) {
        filteredTrialDataArms.add(trialDataArm);
      }
    }

    return filteredTrialDataArms;
  }

  private AbstractNetworkMetaAnalysisProblemEntry buildEntry(String studyName, Integer treatmentId, Measurement measurement) {
    Integer sampleSize = measurement.getSampleSize();
    if (measurement.getMean() != null) {
      Double mu = measurement.getMean();
      Double sigma = measurement.getStdDev();
      return new ContinuousNetworkMetaAnalysisProblemEntry(studyName, treatmentId, sampleSize, mu, sigma);
    } else if (measurement.getRate() != null) {
      Integer rate = measurement.getRate();
      return new RateNetworkMetaAnalysisProblemEntry(studyName, treatmentId, sampleSize, rate);
    }
    throw new RuntimeException("unknown measurement type");
  }

  private List<AbstractIntervention> filterExcludedInterventions(List<AbstractIntervention> interventions, List<InterventionInclusion> inclusions) {
    List<AbstractIntervention> filteredInterventions = new ArrayList<>();

    Map<Integer, InterventionInclusion> inclusionMap = new HashMap<>(inclusions.size());
    for (InterventionInclusion interventionInclusion : inclusions) {
      inclusionMap.put(interventionInclusion.getInterventionId(), interventionInclusion);
    }

    filteredInterventions.addAll(interventions.stream().
            filter(intervention -> inclusionMap.get(intervention.getId()) != null)
            .collect(Collectors.toList()));

    return filteredInterventions;
  }

  private SingleStudyBenefitRiskProblem getSingleStudyBenefitRiskProblem(Project project, SingleStudyBenefitRiskAnalysis analysis) throws ResourceDoesNotExistException, URISyntaxException, ReadValueException, InvalidTypeForDoseCheckException {
    final Set<URI> outcomeUris = analysis.getSelectedOutcomes()
            .stream().map(Outcome::getSemanticOutcomeUri).collect(Collectors.toSet());
    final List<AbstractIntervention> interventions = interventionRepository.query(project.getId());
    final Map<Integer, AbstractIntervention> interventionMap = interventions
            .stream().collect(Collectors.toMap(AbstractIntervention::getId, Function.identity()));
    final Map<URI, Outcome> outcomesByUriMap = analysis.getSelectedOutcomes()
            .stream().collect(Collectors.toMap(Outcome::getSemanticOutcomeUri, Function.identity()));

    final Set<URI> alternativeUris = analysis.getInterventionInclusions()
            .stream().map(intervention -> interventionMap.get(intervention.getInterventionId()).getSemanticInterventionUri())
            .collect(Collectors.toSet());
    final Set<Integer> interventionIds = analysis.getInterventionInclusions().stream().map(InterventionInclusion::getInterventionId).collect(Collectors.toSet());

    final Map<String, AbstractIntervention> alternativeToInterventionMap = interventions.stream()
            .collect(Collectors.toMap(ai -> ai.getId().toString(), Function.identity()));

    final List<AbstractIntervention> includedInterventions = analysisService.getIncludedInterventions(analysis);

    final String versionedUuid = mappingService.getVersionedUuid(project.getNamespaceUid());
    final List<TrialDataStudy> singleStudyMeasurements = triplestoreService.getSingleStudyData(versionedUuid,
            analysis.getStudyGraphUri(), project.getDatasetVersion(), outcomeUris, alternativeUris);
    TrialDataStudy trialDataStudy = singleStudyMeasurements.get(0);

    Map<Integer, AlternativeEntry> alternatives = new HashMap<>();
    Map<URI, CriterionEntry> criteria = new HashMap<>();
    Set<Pair<Measurement, Integer>> measurementDrugInstancePairs = new HashSet<>();
    for (TrialDataArm arm : trialDataStudy.getTrialDataArms()) {
      List<Measurement> measurements = arm.getMeasurements();
      Set<AbstractIntervention> matchingIncludedInterventions = triplestoreService.findMatchingIncludedInterventions(includedInterventions, arm);
      if (matchingIncludedInterventions.size() == 1) {
        Integer matchedProjectInterventionId = matchingIncludedInterventions.iterator().next().getId();
        for (Measurement measurement : measurements) {
          measurementDrugInstancePairs.add(Pair.of(measurement, matchedProjectInterventionId));
          CriterionEntry criterionEntry = createCriterionEntry(measurement, outcomesByUriMap.get(measurement.getVariableConceptUri()));
          criteria.put(measurement.getVariableUri(), criterionEntry);
        }

        arm.setMatchedProjectInterventionIds(ImmutableSet.of(matchedProjectInterventionId));
        String alternativeName = alternativeToInterventionMap.get(matchedProjectInterventionId.toString()).getName();
        alternatives.put(matchedProjectInterventionId, new AlternativeEntry(matchedProjectInterventionId, alternativeName));
      } else if (matchingIncludedInterventions.size() > 1) {
        throw new RuntimeException("too many matched interventions for arm when creating problem");
      }

    }
    List<AbstractMeasurementEntry> performanceTable = performanceTableBuilder.build(measurementDrugInstancePairs);
    return new SingleStudyBenefitRiskProblem(analysis.getTitle(), alternatives, criteria, performanceTable);
  }

  private CriterionEntry createCriterionEntry(Measurement measurement, Outcome outcome) throws EnumConstantNotPresentException {
    List<Double> scale;
    if (measurement.getRate() != null) { // rate measurement
      scale = Arrays.asList(0.0, 1.0);
    } else if (measurement.getMean() != null) { // continuous measurement
      scale = Arrays.asList(null, null);
    } else {
      throw new RuntimeException("Invalid measurement");
    }
    // NB: partialvaluefunctions to be filled in by MCDA component, left null here
    return new CriterionEntry(measurement.getVariableUri(), outcome.getName(), scale, null);
  }


}

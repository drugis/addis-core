package org.drugis.addis.problems.service.impl;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.drugis.addis.analyses.model.*;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.covariates.Covariate;
import org.drugis.addis.covariates.CovariateRepository;
import org.drugis.addis.exception.ProblemCreationException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.interventions.model.SingleIntervention;
import org.drugis.addis.interventions.repository.InterventionRepository;
import org.drugis.addis.interventions.service.InterventionService;
import org.drugis.addis.models.Model;
import org.drugis.addis.models.service.ModelService;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.outcomes.repository.OutcomeRepository;
import org.drugis.addis.patavitask.PataviTask;
import org.drugis.addis.patavitask.repository.PataviTaskRepository;
import org.drugis.addis.patavitask.repository.UnexpectedNumberOfResultsException;
import org.drugis.addis.problems.model.*;
import org.drugis.addis.problems.service.ProblemService;
import org.drugis.addis.problems.service.model.*;
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
  private ModelService modelService;

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
  public AbstractProblem getProblem(Integer projectId, Integer analysisId) throws ResourceDoesNotExistException, ProblemCreationException {
    Project project = projectRepository.get(projectId);
    AbstractAnalysis analysis = analysisRepository.get(analysisId);
    try {
      if (analysis instanceof NetworkMetaAnalysis) {
        return getNetworkMetaAnalysisProblem(project, (NetworkMetaAnalysis) analysis);
      } else if (analysis instanceof BenefitRiskAnalysis) {
        return getBenefitRiskAnalysisProblem(project, (BenefitRiskAnalysis) analysis);
      }
    } catch (URISyntaxException | SQLException | IOException | ReadValueException |
            UnexpectedNumberOfResultsException e) {
      throw new ProblemCreationException(e);
    }
    throw new RuntimeException("unknown analysis type");
  }

  private BenefitRiskProblem getBenefitRiskAnalysisProblem(Project project, BenefitRiskAnalysis analysis) throws
          SQLException, IOException, UnexpectedNumberOfResultsException, URISyntaxException {
    final Set<Integer> networkModelIds = getInclusionIdsWithBaseline(analysis.getBenefitRiskNMAOutcomeInclusions(),
            BenefitRiskNMAOutcomeInclusion::getModelId);
    final Set<Integer> outcomeIds = getInclusionIdsWithBaseline(analysis.getBenefitRiskNMAOutcomeInclusions(),
            BenefitRiskNMAOutcomeInclusion::getOutcomeId);
    outcomeIds.addAll(analysis.getBenefitRiskStudyOutcomeInclusions().stream().map(
            BenefitRiskStudyOutcomeInclusion::getOutcomeId).collect(Collectors.toList()));

    final List<Model> models = modelService.get(networkModelIds);
    final Map<Integer, Model> modelMap = models.stream()
            .collect(Collectors.toMap(Model::getId, Function.identity()));
    List<Outcome> outcomes = outcomeRepository.get(project.getId(), outcomeIds);
    final Map<Integer, Outcome> outcomesById = outcomes.stream()
            .collect(Collectors.toMap(Outcome::getId, Function.identity()));

    List<URI> taskUris = models.stream().map(Model::getTaskUrl)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    final Map<URI, PataviTask> pataviTaskMap = pataviTaskRepository.findByUrls(taskUris)
            .stream()
            .collect(Collectors.toMap(PataviTask::getSelf, Function.identity()));
    final Map<Integer, PataviTask> tasksByModelId = models.stream()
            .filter(model -> model.getTaskUrl() != null)
            .collect(Collectors.toMap(Model::getId, m -> pataviTaskMap.get(m.getTaskUrl())));

    final Map<URI, JsonNode> resultsByTaskUrl = pataviTaskRepository.getResults(taskUris);
    final List<BenefitRiskNMAOutcomeInclusion> inclusionsWithBaselineAndModelResults = analysis.getBenefitRiskNMAOutcomeInclusions().stream()
            .filter(mbrOutcomeInclusion -> mbrOutcomeInclusion.getBaseline() != null)
            .filter(mbrOutcomeInclusion -> {
              URI taskUrl = modelMap.get(mbrOutcomeInclusion.getModelId()).getTaskUrl();
              return taskUrl != null && resultsByTaskUrl.get(taskUrl) != null;
            })
            .collect(Collectors.toList());
    final List<InterventionInclusion> interventionInclusions = analysis.getInterventionInclusions();
    final Set<AbstractIntervention> interventions = interventionRepository.query(analysis.getProjectId());
    final Map<Integer, AbstractIntervention> interventionMap = interventions.stream()
            .collect(Collectors.toMap(AbstractIntervention::getId, Function.identity()));
    final Set<AbstractIntervention> includedAlternatives = interventionInclusions
            .stream()
            .map(inclusion -> interventionMap.get(inclusion.getInterventionId()))
            .collect(Collectors.toSet());

    Map<URI, CriterionEntry> criteriaWithBaseline = new HashMap<>();

    outcomes.forEach(outcome -> {
      Optional<BenefitRiskNMAOutcomeInclusion> outcomeInclusion = inclusionsWithBaselineAndModelResults.stream()
              .filter(mbrOutcomeInclusion -> mbrOutcomeInclusion.getOutcomeId().equals(outcome.getId()))
              .findFirst();
      if (outcomeInclusion.isPresent()) {
        Model model = modelMap.get(outcomeInclusion.get().getModelId());
        URI modelURI = getModelUri(model, project);
        if (model.getLikelihood().equals("binom")) {
          criteriaWithBaseline.put(outcome.getSemanticOutcomeUri(), new CriterionEntry(outcome.getSemanticOutcomeUri().toString(),
                  outcome.getName(), Arrays.asList(0d, 1d), null, "proportion", "meta analysis", modelURI));
        } else {
          criteriaWithBaseline.put(outcome.getSemanticOutcomeUri(), new CriterionEntry(outcome.getSemanticOutcomeUri().toString(),
                  outcome.getName(), "meta analysis", modelURI));
        }
      }
    });

    Map<String, AlternativeEntry> alternatives = includedAlternatives
            .stream()
            .collect(Collectors.toMap(includedAlternative -> includedAlternative.getId().toString(),
                    includedAlternative -> new AlternativeEntry(includedAlternative.getId(), includedAlternative.getName())));

    final Map<String, AbstractIntervention> includedInterventionsByName = includedAlternatives
            .stream()
            .collect(Collectors.toMap(AbstractIntervention::getName, Function.identity()));
    final Map<String, AbstractIntervention> includedInterventionsById = includedAlternatives
            .stream()
            .collect(Collectors.toMap(i -> i.getId().toString(), Function.identity()));

    List<AbstractMeasurementEntry> performanceTable =
            inclusionsWithBaselineAndModelResults.stream()
                    .map(outcomeInclusion -> getPerformanceTableEntry(modelMap,
                            outcomesById, tasksByModelId, resultsByTaskUrl, includedInterventionsById,
                            includedInterventionsByName, outcomeInclusion))
                    .collect(Collectors.toList());

    List<SingleStudyBenefitRiskProblem> singleStudyProblems = analysis.getBenefitRiskStudyOutcomeInclusions().stream()
            .map(inclusion -> getSingleStudyBenefitRiskProblem(project, inclusion.getStudyGraphUri(),
                    outcomesById.get(inclusion.getOutcomeId()), includedAlternatives))
            .collect(Collectors.toList());
    singleStudyProblems.forEach(problem -> {
      criteriaWithBaseline.putAll(problem.getCriteria());
      alternatives.putAll(problem.getAlternatives());
      performanceTable.addAll(problem.getPerformanceTable());
    });
    return new BenefitRiskProblem(criteriaWithBaseline, alternatives, performanceTable);
  }

  private URI getModelUri(Model model, Project project) {
    Integer modelAnalysisId = model.getAnalysisId();
    Integer modelProjectId = project.getId();
    Integer modelOwnerId = project.getOwner().getId();

    return URI.create("/#/users/" + modelOwnerId + "/projects/" + modelProjectId +
            "/nma/" + modelAnalysisId + "/models/" + model.getId());
  }

  @SuppressWarnings("SuspiciousNameCombination")
  private AbstractMeasurementEntry getPerformanceTableEntry(
          Map<Integer, Model> modelMap,
          Map<Integer, Outcome> outcomesById,
          Map<Integer, PataviTask> tasksByModelId,
          Map<URI, JsonNode> resultsByTaskUrl,
          Map<String, AbstractIntervention> interventionsByKey,
          Map<String, AbstractIntervention> includedInterventionsByName,
          BenefitRiskNMAOutcomeInclusion outcomeInclusion) {
    AbstractBaselineDistribution tempBaseline = null;
    try {
      tempBaseline = objectMapper.readValue(outcomeInclusion.getBaseline(), AbstractBaselineDistribution.class);
    } catch (IOException e) {
      e.printStackTrace();
    }
    final AbstractBaselineDistribution baselineDistribution = tempBaseline;
    assert baselineDistribution != null;

    URI taskUrl = tasksByModelId.get(outcomeInclusion.getModelId()).getSelf();
    JsonNode taskResults = resultsByTaskUrl.get(taskUrl);

    Map<Integer, MultiVariateDistribution> distributionByInterventionId = null;
    try {
      distributionByInterventionId = objectMapper.readValue(
              taskResults.get("multivariateSummary").toString(),
              new TypeReference<Map<Integer, MultiVariateDistribution>>() {
              });
    } catch (IOException e) {
      e.printStackTrace();
    }
    assert distributionByInterventionId != null;

    AbstractIntervention baselineIntervention = includedInterventionsByName.get(baselineDistribution.getName());
    MultiVariateDistribution distribution = distributionByInterventionId.get(baselineIntervention.getId());

    Map<String, Double> mu = distribution.getMu().entrySet().stream()
            .collect(Collectors.toMap(
                    e -> {
                      String key = e.getKey();
                      return key.substring(key.lastIndexOf('.') + 1);
                    },
                    Map.Entry::getValue));

    // filter mu
    mu = mu.entrySet().stream().filter(m -> interventionsByKey.containsKey(m.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    //add baseline to mu
    String baselineInterventionId = baselineIntervention.getId().toString();
    mu.put(baselineInterventionId, 0.0);

    List<String> rowNames = new ArrayList<>();
    rowNames.addAll(mu.keySet());

    // place baseline at the front of the list
    //noinspection ComparatorMethodParameterNotUsed
    rowNames.sort((rn1, rn2) -> rn1.equals(includedInterventionsByName.get(baselineDistribution.getName()).getId().toString()) ? -1 : 0);

    Map<Pair<String, String>, Double> dataMap = new HashMap<>();

    final Map<String, Map<String, Double>> sigma = distribution.getSigma();
    for (String interventionY : rowNames) {
      rowNames
              .stream()
              .filter(interventionX ->
                      !interventionX.equals(baselineInterventionId) && !interventionY.equals(baselineInterventionId))
              .forEach(interventionX -> {
                Double value = sigma
                        .get("d." + baselineInterventionId + '.' + interventionX)
                        .get("d." + baselineInterventionId + '.' + interventionY);
                dataMap.put(new ImmutablePair<>(interventionX, interventionY), value);
                dataMap.put(new ImmutablePair<>(interventionY, interventionX), value);
              });
    }

    final List<List<Double>> data = new ArrayList<>(rowNames.size());

    // setup data structure and init with null values
    for (int i = 0; i < rowNames.size(); ++i) {
      List<Double> row = new ArrayList<>(rowNames.size());
      for (int j = 0; j < rowNames.size(); ++j) {
        row.add(0.0);
      }
      data.add(row);
    }

    rowNames.forEach(rowName ->
            rowNames
                    .stream()
                    .filter(colName -> !baselineInterventionId.equals(rowName) && !baselineInterventionId.equals(colName))
                    .forEach(colName -> data
                            .get(rowNames.indexOf(rowName))
                            .set(rowNames.indexOf(colName), dataMap.get(ImmutablePair.of(rowName, colName))))
    );

    CovarianceMatrix cov = new CovarianceMatrix(rowNames, rowNames, data);
    Relative relative = new Relative("dmnorm", mu, cov);
    RelativePerformanceParameters parameters =
            new RelativePerformanceParameters(outcomeInclusion.getBaseline(), relative);
    String modelLinkType = modelMap.get(outcomeInclusion.getModelId()).getLink();

    String modelPerformanceType;
    if (!Model.LINK_IDENTITY.equals(modelLinkType)) {
      modelPerformanceType = "relative-" + modelLinkType + "-normal";
    } else {
      modelPerformanceType = "relative-normal";
    }

    RelativePerformance performance = new RelativePerformance(modelPerformanceType, parameters);

    return new RelativePerformanceEntry(outcomesById.get(outcomeInclusion.getOutcomeId()).
            getSemanticOutcomeUri().toString(), performance);
  }

  @Override
  public NetworkMetaAnalysisProblem applyModelSettings(NetworkMetaAnalysisProblem problem, Model model) {
    List<AbstractNetworkMetaAnalysisProblemEntry> entries = problem.getEntries();
    if (model.getSensitivity() != null && model.getSensitivity().get("omittedStudy") != null) {
      String study = (String) model.getSensitivity().get("omittedStudy");
      entries = problem
              .getEntries()
              .stream()
              .filter(e -> !Objects.equals(e.getStudy(), study)) // remove omitted studies
              .collect(Collectors.toList());
    }
    return new NetworkMetaAnalysisProblem(entries, problem.getTreatments(), problem.getStudyLevelCovariates());
  }

  private Set<Integer> getInclusionIdsWithBaseline(List<BenefitRiskNMAOutcomeInclusion> outcomeInclusions,
                                                   ToIntFunction<BenefitRiskNMAOutcomeInclusion> idSelector) {
    return outcomeInclusions.stream().mapToInt(idSelector).boxed().collect(Collectors.toSet());
  }

  private NetworkMetaAnalysisProblem getNetworkMetaAnalysisProblem(Project project, NetworkMetaAnalysis analysis) throws
          URISyntaxException, ReadValueException, ResourceDoesNotExistException {
    // create treatment entries based only on included interventions
    Set<AbstractIntervention> allProjectInterventions = interventionRepository.query(project.getId());
    Set<AbstractIntervention> includedInterventions = onlyIncludedInterventions(allProjectInterventions, analysis.getInterventionInclusions());
    List<TreatmentEntry> treatments = includedInterventions.stream()
            .map(intervention -> new TreatmentEntry(intervention.getId(), intervention.getName()))
            .collect(Collectors.toList());

    Map<Integer, Covariate> projectCovariatesById = covariateRepository.findByProject(project.getId())
            .stream()
            .collect(Collectors.toMap(Covariate::getId, Function.identity()));
    List<String> includedCovariateKeys = analysis.getCovariateInclusions().stream()
            .map(covariateInclusion -> projectCovariatesById.get(covariateInclusion.getCovariateId()).getDefinitionKey())
            .sorted()
            .collect(Collectors.toList());

    List<TrialDataStudy> trialDataStudies = analysisService.buildEvidenceTable(project.getId(), analysis.getId());

    List<AbstractNetworkMetaAnalysisProblemEntry> entries = new ArrayList<>();

    // create map of (non-default) measurement moment inclusions for the analysis
    final Map<URI, URI> measurementMomentsByStudy = analysis.getIncludedMeasurementMoments()
            .stream().collect(Collectors.toMap(MeasurementMomentInclusion::getStudy, MeasurementMomentInclusion::getMeasurementMoment));

    for (TrialDataStudy trialDataStudy : trialDataStudies) {
      List<TrialDataArm> filteredArms = filterUnmatchedArms(trialDataStudy);
      filteredArms = filterExcludedArms(filteredArms, analysis);
      final URI selectedMeasurementMoment = measurementMomentsByStudy.get(trialDataStudy.getStudyUri()) == null
              ? trialDataStudy.getDefaultMeasurementMoment()
              : measurementMomentsByStudy.get(trialDataStudy.getStudyUri());
      filteredArms = filterArmsWithoutDataAtMM(filteredArms, selectedMeasurementMoment);

      // do not include studies with fewer than two included and matched arms
      if (filteredArms.size() >= 2) {
        entries.addAll(filteredArms.stream()
                .map(trialDataArm -> {
                  Set<Measurement> measurements = trialDataArm.getMeasurementsForMoment(selectedMeasurementMoment);
                  return buildEntry(trialDataStudy.getName(),
                          trialDataArm.getMatchedProjectInterventionIds().iterator().next(), // safe because we filter unmatched arms
                          measurements.iterator().next()); // nma has exactly one measurement
                })
                .collect(Collectors.toList()));
      }
    }

    // if there's an entry with missing standard deviation or samplesize, move everything to standard error
    Boolean isStdErrEntry = entries.stream().anyMatch(entry -> entry instanceof ContinuousStdErrEntry);
    if (isStdErrEntry) {
      entries = entries.stream().map(entry -> {
        if (entry instanceof ContinuousStdErrEntry) {
          return entry;
        }
        ContinuousNetworkMetaAnalysisProblemEntry tmpEntry = (ContinuousNetworkMetaAnalysisProblemEntry) entry;
        Double stdErr = tmpEntry.getStdDev() / Math.sqrt(tmpEntry.getSampleSize());
        return new ContinuousStdErrEntry(tmpEntry.getStudy(), tmpEntry.getTreatment(), tmpEntry.getMean(), stdErr);
      }).collect(Collectors.toList());
    }

    // remove studies without entries from final list
    Map<String, Boolean> studyHasEntries = new HashMap<>();
    entries.forEach(entry -> studyHasEntries.put(entry.getStudy(), true));
    List<TrialDataStudy> studiesWithEntries = trialDataStudies.stream()
            .filter(study -> studyHasEntries.get(study.getName()) != null)
            .collect(Collectors.toList());

    // add covariate values to problem
    Map<String, Map<String, Double>> studyLevelCovariates = null;
    if (includedCovariateKeys.size() > 0) {
      studyLevelCovariates = new HashMap<>(trialDataStudies.size());
      Map<String, Covariate> covariatesByKey = covariateRepository.findByProject(project.getId())
              .stream()
              .collect(Collectors.toMap(Covariate::getDefinitionKey, Function.identity()));
      for (TrialDataStudy trialDataStudy : studiesWithEntries) {
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

  private List<TrialDataArm> filterArmsWithoutDataAtMM(List<TrialDataArm> filteredArms, URI selectedMeasurementMoment) {
    return filteredArms.stream()
            .filter(arm -> arm.getMeasurementsForMoment(selectedMeasurementMoment) != null)
            .collect(Collectors.toList());
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

    armExclusionTrialverseIds.addAll(armExclusions.stream()
            .map(ArmExclusion::getTrialverseUid).collect(Collectors.toList()));

    filteredTrialDataArms.addAll(trialDataArms.stream()
            .filter(trialDataArm -> !armExclusionTrialverseIds.contains(trialDataArm.getUri()))
            .collect(Collectors.toList()));

    return filteredTrialDataArms;
  }

  private AbstractNetworkMetaAnalysisProblemEntry buildEntry(String studyName, Integer treatmentId, Measurement measurement) {
    Integer sampleSize = measurement.getSampleSize();
    if (measurement.getMeasurementTypeURI().equals(CONTINUOUS_TYPE_URI)) {
      Double mu = measurement.getMean();
      Double sigma = measurement.getStdDev();
      Double stdErr = measurement.getStdErr();
      if (sigma != null && sampleSize != null) {
        return new ContinuousNetworkMetaAnalysisProblemEntry(studyName, treatmentId, sampleSize, mu, sigma);
      } else {
        return new ContinuousStdErrEntry(studyName, treatmentId, mu, stdErr);
      }
    } else if (measurement.getMeasurementTypeURI().equals(DICHOTOMOUS_TYPE_URI)) {
      Integer rate = measurement.getRate();
      return new RateNetworkMetaAnalysisProblemEntry(studyName, treatmentId, sampleSize, rate);
    }
    throw new RuntimeException("unknown measurement type");
  }

  private Set<AbstractIntervention> onlyIncludedInterventions(Set<AbstractIntervention> interventions, List<InterventionInclusion> inclusions) {
    Set<AbstractIntervention> filteredInterventions = new HashSet<>();

    Map<Integer, InterventionInclusion> inclusionMap = new HashMap<>(inclusions.size());
    for (InterventionInclusion interventionInclusion : inclusions) {
      inclusionMap.put(interventionInclusion.getInterventionId(), interventionInclusion);
    }

    filteredInterventions.addAll(interventions.stream().
            filter(intervention -> inclusionMap.get(intervention.getId()) != null)
            .collect(Collectors.toList()));

    return filteredInterventions;
  }

  private SingleStudyBenefitRiskProblem getSingleStudyBenefitRiskProblem(Project project, URI studyGraphUri,
                                                                         Outcome outcome,
                                                                         Set<AbstractIntervention> includedInterventions) {
    final Set<URI> outcomeUris = Sets.newHashSet(outcome.getSemanticOutcomeUri());
    final Set<URI> alternativeUris = getSingleAlternativeUris(includedInterventions);
    final Map<Integer, AbstractIntervention> alternativeToInterventionMap = includedInterventions.stream()
            .collect(Collectors.toMap(AbstractIntervention::getId, Function.identity()));

    final String versionedUuid = mappingService.getVersionedUuid(project.getNamespaceUid());
    List<TrialDataStudy> singleStudyMeasurements = null;
    try {
      singleStudyMeasurements = triplestoreService.getSingleStudyData(versionedUuid,
              studyGraphUri, project.getDatasetVersion(), outcomeUris, alternativeUris);
    } catch (ReadValueException e) {
      e.printStackTrace();
    }
    TrialDataStudy trialDataStudy = singleStudyMeasurements.get(0);
    Map<String, AlternativeEntry> alternatives = new HashMap<>();
    Map<URI, CriterionEntry> criteria = new HashMap<>();
    Set<Pair<Measurement, Integer>> measurementDrugInstancePairs = new HashSet<>();
    for (TrialDataArm arm : trialDataStudy.getTrialDataArms()) {
      Set<Measurement> measurements = arm.getMeasurementsForMoment(trialDataStudy.getDefaultMeasurementMoment());
      Set<AbstractIntervention> matchingIncludedInterventions = triplestoreService.findMatchingIncludedInterventions(includedInterventions, arm);
      if (matchingIncludedInterventions.size() == 1) {
        Integer matchedProjectInterventionId = matchingIncludedInterventions.iterator().next().getId();
        for (Measurement measurement : measurements) {
          measurementDrugInstancePairs.add(Pair.of(measurement, matchedProjectInterventionId));
          CriterionEntry criterionEntry = createCriterionEntry(project, measurement, outcome, studyGraphUri);
          criteria.put(measurement.getVariableConceptUri(), criterionEntry);
        }

        arm.setMatchedProjectInterventionIds(ImmutableSet.of(matchedProjectInterventionId));
        AbstractIntervention intervention = alternativeToInterventionMap.get(matchedProjectInterventionId);

        alternatives.put(intervention.getId().toString(), new AlternativeEntry(matchedProjectInterventionId, intervention.getName()));
      } else if (matchingIncludedInterventions.size() > 1) {
        throw new RuntimeException("too many matched interventions for arm when creating problem");
      }

    }
    List<AbstractMeasurementEntry> performanceTable = performanceTableBuilder.build(measurementDrugInstancePairs);
    return new SingleStudyBenefitRiskProblem(alternatives, criteria, performanceTable);
  }

  private Set<URI> getSingleAlternativeUris(Set<AbstractIntervention> includedInterventions) {
    Set<SingleIntervention> singleIncludedInterventions = null;
    try {
      singleIncludedInterventions = analysisService.getSingleInterventions(includedInterventions);
    } catch (ResourceDoesNotExistException e) {
      e.printStackTrace();
    }

    return singleIncludedInterventions.stream()
            .map(SingleIntervention::getSemanticInterventionUri)
            .collect(Collectors.toSet());
  }

  private CriterionEntry createCriterionEntry(Project project, Measurement measurement, Outcome outcome, URI studyGraphUri) throws EnumConstantNotPresentException {
    List<Double> scale;
    String unitOfMeasurement;
    if (measurement.getRate() != null) { // rate measurement
      scale = Arrays.asList(0.0, 1.0);
      unitOfMeasurement = "probability";
    } else if (measurement.getMean() != null) { // continuous measurement
      scale = Arrays.asList(null, null);
      unitOfMeasurement = null;
    } else {
      throw new RuntimeException("Invalid measurement");
    }
    // NB: partialvaluefunctions to be filled in by MCDA component, left null here
    return new CriterionEntry(measurement.getVariableUri().toString(), outcome.getName(), scale, null,
            unitOfMeasurement, "study",
            URI.create("/#/users/" + project.getOwner().getId() +
                    "/datasets/" + project.getNamespaceUid() +
                    "/versions/" + project.getDatasetVersion().toString().split("/versions/")[1]+
                    "/studies/" + studyGraphUri.toString().split("/graphs/")[1] ));
  }

}

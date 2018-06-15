package org.drugis.addis.problems.service.impl;


import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
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
import org.drugis.addis.problems.service.HostURLCache;
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
import org.drugis.trialverse.util.service.UuidService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.*;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;

/**
 * Created by daan on 3/21/14.
 */
@Service
public class ProblemServiceImpl implements ProblemService {

  @Inject
  private ProjectRepository projectRepository;

  @Inject
  private SingleStudyPerformanceTableBuilder singleStudyPerformanceTableBuilder;

  @Inject
  private NetworkPerformanceTableBuilder networkPerformanceTableBuilder;

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

  @Inject
  private UuidService uuidService;

  @Inject
  private HostURLCache hostURLCache;

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

    final Map<Integer, Model> modelsById = getModelsById(analysis);
    final Map<Integer, Outcome> outcomesById = getOutcomesById(project.getId(), analysis);
    final Map<Integer, JsonNode> resultsByModelId = getPataviResultsByModelId(modelsById.values());
    final Map<Integer, BenefitRiskNMAOutcomeInclusion> usableNMAInclusionsByOutcomeId = findUsableNMAInclusions(analysis, modelsById, resultsByTaskUrl);
    final Set<AbstractIntervention> includedInterventions = getIncludedInterventions(analysis);

    // output
    final Map<URI, CriterionEntry> criteriaWithBaseline = buildNMACriteriaWithBaseline(project, modelsById, outcomesById, usableNMAInclusionsByOutcomeId);
    final Map<String, AlternativeEntry> alternativesById = getAlternativesById(includedInterventions);

    Map<String, DataSourceEntry> dataSourcesByOutcomeId = getDataSourcesByOutcomeId(criteriaWithBaseline);

    // output
    List<AbstractMeasurementEntry> performanceTable = networkPerformanceTableBuilder.build(usableNMAInclusionsByOutcomeId.values(),
        modelsById, outcomesById, dataSourcesByOutcomeId, tasksByModelId, resultsByTaskUrl, includedInterventions);

    List<SingleStudyBenefitRiskProblem> singleStudyProblems = getSingleStudyProblems(project, analysis, outcomesById, includedInterventions);

    singleStudyProblems.forEach(problem -> {
      criteriaWithBaseline.putAll(problem.getCriteria());
      alternativesById.putAll(problem.getAlternatives());
      performanceTable.addAll(problem.getPerformanceTable());
    });
    return new BenefitRiskProblem(criteriaWithBaseline, alternativesById, performanceTable);
  }

  private List<SingleStudyBenefitRiskProblem> getSingleStudyProblems(Project project, BenefitRiskAnalysis analysis, Map<Integer, Outcome> outcomesById, Set<AbstractIntervention> includedInterventions) {
    return analysis.getBenefitRiskStudyOutcomeInclusions().stream()
        .collect(groupingBy(BenefitRiskStudyOutcomeInclusion::getStudyGraphUri))
        .entrySet().stream()
        .map(entry -> {
          URI studyURI = entry.getKey();
          List<BenefitRiskStudyOutcomeInclusion> studyInclusions = entry.getValue();
          Set<Outcome> outcomes = studyInclusions.stream()
              .map(inclusion -> outcomesById.get(inclusion.getOutcomeId()))
              .collect(toSet());
          return getSingleStudyBenefitRiskProblem(project, studyURI, outcomes, includedInterventions);
        })
        .collect(toList());
  }

  private Map<String, DataSourceEntry> getDataSourcesByOutcomeId(Map<URI, CriterionEntry> criteriaWithBaseline) {
    return criteriaWithBaseline.values().stream()
        .collect(toMap(CriterionEntry::getCriterion, criterionEntry -> criterionEntry.getDataSources().get(0)));
  }

  private Map<String, AlternativeEntry> getAlternativesById(Set<AbstractIntervention> includedInterventions) {
    return includedInterventions
        .stream()
        .collect(toMap(includedAlternative -> includedAlternative.getId().toString(),
            includedAlternative -> new AlternativeEntry(includedAlternative.getId(), includedAlternative.getName())));
  }

  private Map<Integer, BenefitRiskNMAOutcomeInclusion> findUsableNMAInclusions(BenefitRiskAnalysis analysis, Map<Integer, Model> modelsById, Map<URI, JsonNode> resultsByTaskUrl) {
    return analysis.getBenefitRiskNMAOutcomeInclusions().stream()
        .filter(mbrOutcomeInclusion -> mbrOutcomeInclusion.getBaseline() != null)
        .filter(mbrOutcomeInclusion -> {
          URI taskUrl = modelsById.get(mbrOutcomeInclusion.getModelId()).getTaskUrl();
          return taskUrl != null && resultsByTaskUrl.get(taskUrl) != null;
        })
        .collect(toMap(BenefitRiskNMAOutcomeInclusion::getOutcomeId, identity()));
  }

  private Map<URI, CriterionEntry> buildNMACriteriaWithBaseline(Project project,
                                                                Map<Integer, Model> modelsById,
                                                                Map<Integer, Outcome> outcomesById,
                                                                Map<Integer, BenefitRiskNMAOutcomeInclusion> inclusionsWithBaselineAndModelResults) {
    Map<URI, CriterionEntry> criteriaWithBaseline = new HashMap<>();

    outcomesById.values().forEach(outcome -> {
      BenefitRiskNMAOutcomeInclusion outcomeInclusion = inclusionsWithBaselineAndModelResults.get(outcome.getId());
      if (outcomeInclusion != null) {
        Model model = modelsById.get(outcomeInclusion.getModelId());
        URI modelURI = getModelUri(project, model);
        CriterionEntry criterionEntry;
        if ("binom".equals(model.getLikelihood())) {
          DataSourceEntry dataSource = new DataSourceEntry(uuidService.generate(), Arrays.asList(0d, 1d),
              /* pvf */ null, "meta analysis", modelURI);
          criterionEntry = new CriterionEntry(outcome.getSemanticOutcomeUri().toString(), Collections.singletonList(dataSource),
              outcome.getName(), "proportion");
        } else {
          DataSourceEntry dataSource = new DataSourceEntry(uuidService.generate(), "meta analysis", modelURI);
          criterionEntry = new CriterionEntry(outcome.getSemanticOutcomeUri().toString(), Collections.singletonList(dataSource), outcome.getName());
        }
        criteriaWithBaseline.put(outcome.getSemanticOutcomeUri(), criterionEntry);
      }
    });
    return criteriaWithBaseline;
  }

  private Set<AbstractIntervention> getIncludedInterventions(BenefitRiskAnalysis analysis) {
    final List<InterventionInclusion> interventionInclusions = analysis.getInterventionInclusions();
    final Set<AbstractIntervention> interventions = interventionRepository.query(analysis.getProjectId());
    final Map<Integer, AbstractIntervention> interventionMap = interventions.stream()
        .collect(toMap(AbstractIntervention::getId, identity()));
    return interventionInclusions
        .stream()
        .map(inclusion -> interventionMap.get(inclusion.getInterventionId()))
        .collect(toSet());
  }

  private Map<Integer, JsonNode> getPataviResultsByModelId(Collection<Model> models) throws IOException, SQLException, UnexpectedNumberOfResultsException, URISyntaxException {
    List<URI> taskUris = models.stream()
        .map(Model::getTaskUrl)
        .filter(Objects::nonNull)
        .collect(toList());
    final Map<URI, PataviTask> pataviTaskMap = pataviTaskRepository.findByUrls(taskUris).stream()
        .collect(toMap(PataviTask::getSelf, identity()));
    Map<Integer, PataviTask> tasksByModelId = models.stream()
        .filter(model -> model.getTaskUrl() != null)
        .collect(toMap(Model::getId, m -> pataviTaskMap.get(m.getTaskUrl())));
    Map<URI, JsonNode> resultsByUrl = pataviTaskRepository.getResults(tasksByModelId.values());
    Set<Map.Entry<URI, JsonNode>> entries = resultsByUrl.entrySet();
    return entries.stream()
        .map(e -> Pair.of(findModelIDForTaskUrl(e.getKey(), models), e.getValue()))
        .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
  }

  private Integer findModelIDForTaskUrl(URI taskUri, Collection<Model> models) {
    Optional<Model> model = models.stream()
        .filter(m -> m.getTaskUrl().equals(taskUri))
        .findFirst();
    if (!model.isPresent()) {
      throw new RuntimeException("missing model for taskUrl");
    }
    return model.get().getId();
  }

  private Map<Integer, Model> getModelsById(BenefitRiskAnalysis analysis) throws IOException, SQLException {
    final Set<Integer> networkModelIds = getInclusionIdsWithBaseline(analysis.getBenefitRiskNMAOutcomeInclusions(),
        BenefitRiskNMAOutcomeInclusion::getModelId);
    final List<Model> models = modelService.get(networkModelIds);
    return models.stream()
        .collect(toMap(Model::getId, identity()));
  }

  private Map<Integer, Outcome> getOutcomesById(Integer projectId, BenefitRiskAnalysis analysis) {
    final Set<Integer> outcomeIds = getInclusionIdsWithBaseline(analysis.getBenefitRiskNMAOutcomeInclusions(),
        BenefitRiskNMAOutcomeInclusion::getOutcomeId);
    outcomeIds.addAll(analysis.getBenefitRiskStudyOutcomeInclusions().stream().map(
        BenefitRiskStudyOutcomeInclusion::getOutcomeId).collect(toList()));

    List<Outcome> outcomes = outcomeRepository.get(projectId, outcomeIds);
    return outcomes.stream()
        .collect(toMap(Outcome::getId, identity()));
  }

  private URI getModelUri(Project project, Model model) {
    Integer modelAnalysisId = model.getAnalysisId();
    Integer modelProjectId = project.getId();
    Integer modelOwnerId = project.getOwner().getId();
    String hostURL = hostURLCache.get();
    return URI.create(hostURL + "/#/users/" + modelOwnerId + "/projects/" + modelProjectId +
        "/nma/" + modelAnalysisId + "/models/" + model.getId());
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
          .collect(toList());
    }
    return new NetworkMetaAnalysisProblem(entries, problem.getTreatments(), problem.getStudyLevelCovariates());
  }

  private Set<Integer> getInclusionIdsWithBaseline(List<BenefitRiskNMAOutcomeInclusion> outcomeInclusions,
                                                   ToIntFunction<BenefitRiskNMAOutcomeInclusion> idSelector) {
    return outcomeInclusions.stream().mapToInt(idSelector).boxed().collect(toSet());
  }

  private NetworkMetaAnalysisProblem getNetworkMetaAnalysisProblem(Project project, NetworkMetaAnalysis analysis) throws
      URISyntaxException, ReadValueException, ResourceDoesNotExistException {
    // create treatment entries based only on included interventions
    Set<AbstractIntervention> allProjectInterventions = interventionRepository.query(project.getId());
    Set<AbstractIntervention> includedInterventions = onlyIncludedInterventions(allProjectInterventions, analysis.getInterventionInclusions());
    List<TreatmentEntry> treatments = includedInterventions.stream()
        .map(intervention -> new TreatmentEntry(intervention.getId(), intervention.getName()))
        .collect(toList());

    Map<Integer, Covariate> projectCovariatesById = covariateRepository.findByProject(project.getId())
        .stream()
        .collect(toMap(Covariate::getId, identity()));
    List<String> includedCovariateKeys = analysis.getCovariateInclusions().stream()
        .map(covariateInclusion -> projectCovariatesById.get(covariateInclusion.getCovariateId()).getDefinitionKey())
        .sorted()
        .collect(toList());

    List<TrialDataStudy> trialDataStudies = analysisService.buildEvidenceTable(project.getId(), analysis.getId());

    List<AbstractNetworkMetaAnalysisProblemEntry> entries = new ArrayList<>();

    // create map of (non-default) measurement moment inclusions for the analysis
    final Map<URI, URI> measurementMomentsByStudy = analysis.getIncludedMeasurementMoments()
        .stream().collect(toMap(MeasurementMomentInclusion::getStudy, MeasurementMomentInclusion::getMeasurementMoment));

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
            .collect(toList()));
      }
    }

    // if there's an entry with missing standard deviation or sample size, move everything to standard error
    Boolean isStdErrEntry = entries.stream().anyMatch(entry -> entry instanceof ContinuousStdErrEntry);
    if (isStdErrEntry) {
      entries = entries.stream().map(entry -> {
        if (entry instanceof ContinuousStdErrEntry) {
          return entry;
        }
        ContinuousNetworkMetaAnalysisProblemEntry tmpEntry = (ContinuousNetworkMetaAnalysisProblemEntry) entry;
        Double stdErr = tmpEntry.getStdDev() / Math.sqrt(tmpEntry.getSampleSize());
        return new ContinuousStdErrEntry(tmpEntry.getStudy(), tmpEntry.getTreatment(), tmpEntry.getMean(), stdErr);
      }).collect(toList());
    }

    // remove studies without entries from final list
    Map<String, Boolean> studyHasEntries = new HashMap<>();
    entries.forEach(entry -> studyHasEntries.put(entry.getStudy(), true));
    List<TrialDataStudy> studiesWithEntries = trialDataStudies.stream()
        .filter(study -> studyHasEntries.get(study.getName()) != null)
        .collect(toList());

    // add covariate values to problem
    Map<String, Map<String, Double>> studyLevelCovariates = null;
    if (includedCovariateKeys.size() > 0) {
      studyLevelCovariates = new HashMap<>(trialDataStudies.size());
      Map<String, Covariate> covariatesByKey = covariateRepository.findByProject(project.getId())
          .stream()
          .collect(toMap(Covariate::getDefinitionKey, identity()));
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
        .collect(toList());
  }

  private List<TrialDataArm> filterUnmatchedArms(TrialDataStudy trialDataStudy) {
    return trialDataStudy.getTrialDataArms()
        .stream()
        .filter(a -> a.getMatchedProjectInterventionIds().size() > 0)
        .collect(toList());
  }

  private List<TrialDataArm> filterExcludedArms(List<TrialDataArm> trialDataArms, NetworkMetaAnalysis analysis) {
    List<ArmExclusion> armExclusions = analysis.getExcludedArms();
    List<URI> armExclusionTrialverseIds = new ArrayList<>(armExclusions.size());

    armExclusionTrialverseIds.addAll(armExclusions.stream()
        .map(ArmExclusion::getTrialverseUid).collect(toList()));

    return trialDataArms.stream()
        .filter(trialDataArm -> !armExclusionTrialverseIds.contains(trialDataArm.getUri())).collect(toList());
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
    } else if (measurement.getMeasurementTypeURI().equals(SURVIVAL_TYPE_URI)) {
      Integer rate = measurement.getRate();
      Double exposure = measurement.getExposure();
      String timeScale = measurement.getSurvivalTimeScale();
      return new SurvivalEntry(studyName, treatmentId, timeScale, rate, exposure);
    }
    throw new RuntimeException("unknown measurement type");
  }

  private Set<AbstractIntervention> onlyIncludedInterventions(Set<AbstractIntervention> interventions, List<InterventionInclusion> inclusions) {

    Map<Integer, InterventionInclusion> inclusionMap = new HashMap<>(inclusions.size());
    for (InterventionInclusion interventionInclusion : inclusions) {
      inclusionMap.put(interventionInclusion.getInterventionId(), interventionInclusion);
    }

    return interventions.stream().
        filter(intervention -> inclusionMap.get(intervention.getId()) != null).collect(toSet());
  }

  private SingleStudyBenefitRiskProblem getSingleStudyBenefitRiskProblem(Project project, URI studyGraphUri,
                                                                         Set<Outcome> outcomes,
                                                                         Set<AbstractIntervention> includedInterventions) {
    Map<URI, Outcome> outcomesByUri = outcomes.stream().collect(toMap(Outcome::getSemanticOutcomeUri, identity()));
    Map<URI, String> dataSourceIdsByOutcomeUri = outcomes.stream()
        .collect(Collectors.toMap(Outcome::getSemanticOutcomeUri, o -> uuidService.generate()));
    final Set<URI> interventionUris = getSingleInterventionUris(includedInterventions);
    final Map<Integer, AbstractIntervention> interventionsById = includedInterventions.stream()
        .collect(toMap(AbstractIntervention::getId, identity()));

    final String versionedUuid = mappingService.getVersionedUuid(project.getNamespaceUid());
    List<TrialDataStudy> singleStudyMeasurements = null;
    try {
      singleStudyMeasurements = triplestoreService.getSingleStudyData(versionedUuid,
          studyGraphUri, project.getDatasetVersion(), outcomesByUri.keySet(), interventionUris);
    } catch (ReadValueException e) {
      e.printStackTrace();
    }
    assert singleStudyMeasurements != null;
    TrialDataStudy trialDataStudy = singleStudyMeasurements.get(0);
    Map<String, AlternativeEntry> alternatives = new HashMap<>();
    Map<URI, CriterionEntry> criteria = new HashMap<>();
    Set<Triple<Measurement, Integer, String>> measurementsWithCoordinates = new HashSet<>();
    for (TrialDataArm arm : trialDataStudy.getTrialDataArms()) {
      Set<Measurement> measurements = arm.getMeasurementsForMoment(trialDataStudy.getDefaultMeasurementMoment());
      Set<AbstractIntervention> matchingIncludedInterventions = triplestoreService.findMatchingIncludedInterventions(includedInterventions, arm);
      if (matchingIncludedInterventions.size() == 1) {
        Integer matchedProjectInterventionId = matchingIncludedInterventions.iterator().next().getId();
        for (Measurement measurement : measurements) {
          Outcome measuredOutcome = outcomesByUri.get(measurement.getVariableConceptUri());
          String dataSourceId = dataSourceIdsByOutcomeUri.get(measuredOutcome.getSemanticOutcomeUri());
          CriterionEntry criterionEntry = createCriterionEntry(project, measurement, measuredOutcome, dataSourceId, studyGraphUri);
          measurementsWithCoordinates.add(Triple.of(measurement, matchedProjectInterventionId, criterionEntry.getDataSources().get(0).getId()));
          criteria.put(measurement.getVariableConceptUri(), criterionEntry);
        }

        arm.setMatchedProjectInterventionIds(ImmutableSet.of(matchedProjectInterventionId));
        AbstractIntervention intervention = interventionsById.get(matchedProjectInterventionId);

        alternatives.put(intervention.getId().toString(), new AlternativeEntry(matchedProjectInterventionId, intervention.getName()));
      } else if (matchingIncludedInterventions.size() > 1) {
        throw new RuntimeException("too many matched interventions for arm when creating problem");
      }

    }
    List<AbstractMeasurementEntry> performanceTable = singleStudyPerformanceTableBuilder.build(measurementsWithCoordinates);
    return new SingleStudyBenefitRiskProblem(alternatives, criteria, performanceTable);
  }

  private Set<URI> getSingleInterventionUris(Set<AbstractIntervention> includedInterventions) {
    Set<SingleIntervention> singleIncludedInterventions = null;
    try {
      singleIncludedInterventions = analysisService.getSingleInterventions(includedInterventions);
    } catch (ResourceDoesNotExistException e) {
      e.printStackTrace();
    }

    assert singleIncludedInterventions != null;
    return singleIncludedInterventions.stream()
        .map(SingleIntervention::getSemanticInterventionUri)
        .collect(toSet());
  }

  private CriterionEntry createCriterionEntry(Project project, Measurement measurement, Outcome outcome, String dataSourceId, URI studyGraphUri) throws EnumConstantNotPresentException {
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
    Integer ownerId = mappingService.getVersionedUuidAndOwner(project.getNamespaceUid()).getOwnerId();
    URI sourceLink = URI.create("/#/users/" + ownerId +
        "/datasets/" + project.getNamespaceUid() +
        "/versions/" + project.getDatasetVersion().toString().split("/versions/")[1] +
        "/studies/" + studyGraphUri.toString().split("/graphs/")[1]);

    // NB: partial value functions to be filled in by MCDA component, left null here
    DataSourceEntry dataSourceEntry = new DataSourceEntry(dataSourceId, scale, /*pvf*/ null, "study", sourceLink);
    return new CriterionEntry(measurement.getVariableUri().toString(), Collections.singletonList(dataSourceEntry), outcome.getName(), unitOfMeasurement);
  }

}

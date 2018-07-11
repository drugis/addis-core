package org.drugis.addis.problems.service.impl;


import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.tuple.Pair;
import org.drugis.addis.analyses.model.*;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.exception.ProblemCreationException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.models.Model;
import org.drugis.addis.models.service.ModelService;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.outcomes.repository.OutcomeRepository;
import org.drugis.addis.patavitask.PataviTask;
import org.drugis.addis.patavitask.repository.PataviTaskRepository;
import org.drugis.addis.patavitask.repository.UnexpectedNumberOfResultsException;
import org.drugis.addis.problems.model.*;
import org.drugis.addis.problems.service.LinkService;
import org.drugis.addis.problems.service.NetworkMetaAnalysisService;
import org.drugis.addis.problems.service.ProblemService;
import org.drugis.addis.problems.service.SingleStudyBenefitRiskService;
import org.drugis.addis.problems.service.model.AbstractMeasurementEntry;
import org.drugis.addis.projects.Project;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.trialverse.model.trialdata.TrialDataArm;
import org.drugis.addis.trialverse.model.trialdata.TrialDataStudy;
import org.drugis.addis.trialverse.service.impl.ReadValueException;
import org.drugis.addis.util.WebConstants;
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
  private SingleStudyBenefitRiskService singleStudyBenefitRiskService;

  @Inject
  private NetworkMetaAnalysisService networkMetaAnalysisService;

  @Inject
  private NetworkPerformanceTableBuilder networkPerformanceTableBuilder;

  @Inject
  private AnalysisRepository analysisRepository;

  @Inject
  private ModelService modelService;

  @Inject
  private OutcomeRepository outcomeRepository;

  @Inject
  private PataviTaskRepository pataviTaskRepository;

  @Inject
  private AnalysisService analysisService;

  @Inject
  private UuidService uuidService;

  @Inject
  private LinkService linkService;


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
          SQLException, IOException, UnexpectedNumberOfResultsException, URISyntaxException, ResourceDoesNotExistException {

    final Map<Integer, Model> modelsById = getModelsById(analysis);
    final Map<Integer, Outcome> outcomesById = getOutcomesById(project.getId(), analysis);
    final Map<Integer, JsonNode> resultsByModelId = getPataviResultsByModelId(modelsById.values());
    final Set<AbstractIntervention> includedInterventions = analysisService.getIncludedInterventions(analysis);
    final Map<Integer, BenefitRiskNMAOutcomeInclusion> usableNMAInclusionsByOutcomeId = findUsableNMAInclusions(analysis, resultsByModelId);

    // output
    final Map<URI, CriterionEntry> criteriaWithBaseline = buildNMACriteriaWithBaseline(project, modelsById, outcomesById, usableNMAInclusionsByOutcomeId);
    final Map<String, AlternativeEntry> alternativesById = getAlternativesById(includedInterventions);

    Map<String, DataSourceEntry> dataSourcesByOutcomeId = getDataSourcesByOutcomeId(criteriaWithBaseline);

    // output
    List<AbstractMeasurementEntry> performanceTable = networkPerformanceTableBuilder.build(usableNMAInclusionsByOutcomeId.values(),
            modelsById, outcomesById, dataSourcesByOutcomeId, resultsByModelId, includedInterventions);

    List<SingleStudyBenefitRiskProblem> singleStudyProblems = getSingleStudyProblems(project, analysis, outcomesById, includedInterventions);

    singleStudyProblems.forEach(problem -> {
      criteriaWithBaseline.putAll(problem.getCriteria());
      alternativesById.putAll(problem.getAlternatives());
      performanceTable.addAll(problem.getPerformanceTable());
    });
    return new BenefitRiskProblem(WebConstants.SCHEMA_VERSION, criteriaWithBaseline, alternativesById, performanceTable);
  }

  private List<SingleStudyBenefitRiskProblem> getSingleStudyProblems(Project project, BenefitRiskAnalysis analysis, Map<Integer, Outcome> outcomesById, Set<AbstractIntervention> includedInterventions) {
    return analysis.getBenefitRiskStudyOutcomeInclusions().stream()
            .collect(groupingBy(BenefitRiskStudyOutcomeInclusion::getStudyGraphUri))
            .entrySet().stream()
            .map(entry -> {
              URI studyURI = entry.getKey();
              List<BenefitRiskStudyOutcomeInclusion> studyInclusions = entry.getValue();
              Set<Outcome> outcomes = studyInclusions.stream()
                      .map(BenefitRiskStudyOutcomeInclusion::getOutcomeId)
                      .map(outcomesById::get)
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

  private Map<Integer, BenefitRiskNMAOutcomeInclusion> findUsableNMAInclusions(BenefitRiskAnalysis analysis, Map<Integer, JsonNode> resultsByModelId) {
    return analysis.getBenefitRiskNMAOutcomeInclusions().stream()
            .filter(mbrOutcomeInclusion -> mbrOutcomeInclusion.getBaseline() != null)
            .filter(mbrOutcomeInclusion -> {
              Integer modelId = mbrOutcomeInclusion.getModelId();
              return modelId != null && resultsByModelId.get(modelId) != null;
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
        URI modelURI = linkService.getModelSourceLink(project, model);
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

    List<TreatmentEntry> treatments = networkMetaAnalysisService.getTreatments(analysis);

    List<TrialDataStudy> trialDataStudies = analysisService.buildEvidenceTable(project.getId(), analysis.getId());

    List<AbstractNetworkMetaAnalysisProblemEntry> entries = networkMetaAnalysisService.buildPerformanceEntries(analysis, trialDataStudies);

    // remove studies without entries from final list
    List<TrialDataStudy> studiesWithEntries = networkMetaAnalysisService.getStudiesWithEntries(trialDataStudies, entries);

    // add covariate values to problem
    Map<String, Map<String, Double>> studyLevelCovariates = networkMetaAnalysisService.getStudyLevelCovariates(project, analysis, studiesWithEntries);

    return new NetworkMetaAnalysisProblem(entries, treatments, studyLevelCovariates);
  }


  private SingleStudyBenefitRiskProblem getSingleStudyBenefitRiskProblem(Project project, URI studyGraphUri,
                                                                         Set<Outcome> outcomes,
                                                                         Set<AbstractIntervention> includedInterventions) {
    SingleStudyContext context = singleStudyBenefitRiskService.buildContext(project, studyGraphUri, outcomes, includedInterventions);

    TrialDataStudy trialDataStudy = singleStudyBenefitRiskService.getSingleStudyMeasurements(project, studyGraphUri, context);
    List<TrialDataArm> armsWithMatching = singleStudyBenefitRiskService.getArmsWithMatching(includedInterventions, trialDataStudy);

    URI defaultMeasurementMoment = trialDataStudy.getDefaultMeasurementMoment();

    Map<URI, CriterionEntry> criteria = singleStudyBenefitRiskService.getCriteria(
            armsWithMatching, defaultMeasurementMoment, context);
    Map<String, AlternativeEntry> alternatives = singleStudyBenefitRiskService.getAlternatives(
            armsWithMatching, context);
    Set<MeasurementWithCoordinates> measurementsWithCoordinates = singleStudyBenefitRiskService.getMeasurementsWithCoordinates(
            armsWithMatching, defaultMeasurementMoment, context);

    List<AbstractMeasurementEntry> performanceTable = singleStudyBenefitRiskService.buildPerformanceTable(measurementsWithCoordinates);
    return new SingleStudyBenefitRiskProblem(alternatives, criteria, performanceTable);

  }
}

package org.drugis.addis.problems.service.impl;


import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Sets;
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
import org.drugis.addis.patavitask.repository.UnexpectedNumberOfResultsException;
import org.drugis.addis.problems.model.*;
import org.drugis.addis.problems.service.*;
import org.drugis.addis.problems.service.model.AbstractMeasurementEntry;
import org.drugis.addis.projects.Project;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.trialverse.model.trialdata.TrialDataArm;
import org.drugis.addis.trialverse.model.trialdata.TrialDataStudy;
import org.drugis.addis.trialverse.service.impl.ReadValueException;
import org.drugis.addis.util.WebConstants;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;

/**
 * Created by daan on 3/21/14.
 */
@Service
public class ProblemServiceImpl implements ProblemService {

  @Inject
  private AnalysisRepository analysisRepository;

  @Inject
  private AnalysisService analysisService;

  @Inject
  private LinkService linkService;

  @Inject
  private ModelService modelService;

  @Inject
  private NetworkMetaAnalysisService networkMetaAnalysisService;

  @Inject
  private OutcomeRepository outcomeRepository;

  @Inject
  private ProjectRepository projectRepository;

  @Inject
  private SingleStudyBenefitRiskService singleStudyBenefitRiskService;

  @Inject
  private NetworkBenefitRiskPerformanceEntryBuilder networkBenefitRiskPerformanceEntryBuilder;

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

  private NetworkMetaAnalysisProblem getNetworkMetaAnalysisProblem(Project project, NetworkMetaAnalysis analysis) throws
      URISyntaxException, ReadValueException, ResourceDoesNotExistException, IOException {

    List<TreatmentEntry> treatments = networkMetaAnalysisService.getTreatments(analysis);

    List<TrialDataStudy> trialDataStudies = analysisService.buildEvidenceTable(project.getId(), analysis.getId());

    List<AbstractNetworkMetaAnalysisProblemEntry> entries = networkMetaAnalysisService.buildPerformanceEntries(analysis, trialDataStudies);

    // remove studies without entries from final list
    List<TrialDataStudy> studiesWithEntries = networkMetaAnalysisService.getStudiesWithEntries(trialDataStudies, entries);

    // add covariate values to problem
    Map<String, Map<String, Double>> studyLevelCovariates = networkMetaAnalysisService.getStudyLevelCovariates(project, analysis, studiesWithEntries);

    return new NetworkMetaAnalysisProblem(entries, treatments, studyLevelCovariates);
  }

  private BenefitRiskProblem getBenefitRiskAnalysisProblem(Project project, BenefitRiskAnalysis analysis) throws
      SQLException, IOException, UnexpectedNumberOfResultsException, URISyntaxException, ResourceDoesNotExistException {

    final Map<Integer, Outcome> outcomesById = getOutcomesById(project.getId(), analysis);
    final Set<AbstractIntervention> includedInterventions = analysisService.getIncludedInterventions(analysis);

    List<BenefitRiskProblem> problems = getNetworkProblems(project, analysis, outcomesById, includedInterventions);
    problems.addAll(getSingleStudyProblems(project, analysis, outcomesById, includedInterventions));

    Map<URI, CriterionEntry> criteriaWithBaseline = new HashMap<>();
    Map<String, AlternativeEntry> alternativesById = new HashMap<>();
    List<AbstractMeasurementEntry> performanceTable = new ArrayList<>();

    problems.forEach(problem -> {
      criteriaWithBaseline.putAll(problem.getCriteria());
      alternativesById.putAll(problem.getAlternatives());
      performanceTable.addAll(problem.getPerformanceTable());
    });
    return new BenefitRiskProblem(WebConstants.SCHEMA_VERSION, criteriaWithBaseline, alternativesById, performanceTable);
  }

  private Map<Integer, Outcome> getOutcomesById(Integer projectId, BenefitRiskAnalysis analysis) {
    final Set<Integer> outcomeIds = getIdsFromInclusions(analysis.getBenefitRiskNMAOutcomeInclusions(),
        BenefitRiskNMAOutcomeInclusion::getOutcomeId);
    outcomeIds.addAll(analysis.getBenefitRiskStudyOutcomeInclusions().stream().map(
        BenefitRiskStudyOutcomeInclusion::getOutcomeId).collect(toList()));

    List<Outcome> outcomes = outcomeRepository.get(projectId, outcomeIds);
    return outcomes.stream()
        .collect(toMap(Outcome::getId, identity()));
  }

  private Map<Integer, Model> getModelsById(BenefitRiskAnalysis analysis) throws IOException, SQLException {
    final Set<Integer> networkModelIds = getIdsFromInclusions(analysis.getBenefitRiskNMAOutcomeInclusions(),
        BenefitRiskNMAOutcomeInclusion::getModelId);
    final List<Model> models = modelService.get(networkModelIds);
    return models.stream()
        .collect(toMap(Model::getId, identity()));
  }

  private List<BenefitRiskProblem> getNetworkProblems(Project project, BenefitRiskAnalysis analysis, Map<Integer, Outcome> outcomesById, Set<AbstractIntervention> includedInterventions) throws IOException, SQLException, UnexpectedNumberOfResultsException, URISyntaxException {
    if (analysis.getBenefitRiskNMAOutcomeInclusions().size() == 0) {
      return new ArrayList<>();
    }
    final Map<Integer, Model> modelsById = getModelsById(analysis);
    Collection<Model> models = Sets.newHashSet(modelsById.values());
    final Map<Integer, JsonNode> resultsByModelId = networkMetaAnalysisService.getPataviResultsByModelId(models);
    return analysis.getBenefitRiskNMAOutcomeInclusions().stream()
        .filter(inclusion -> inclusion.getBaseline() != null)
        .filter(inclusion -> inclusion.getModelId() != null && resultsByModelId.get(inclusion.getModelId()) != null)
        .map(inclusion -> {
          Outcome outcome = outcomesById.get(inclusion.getOutcomeId());
          Model model = modelsById.get(inclusion.getModelId());
          JsonNode pataviResults = resultsByModelId.get(inclusion.getModelId());
          return new NMAInclusionWithResults(outcome, model, pataviResults, includedInterventions, inclusion.getBaseline());
        })
        .map(inclusion -> getNetworkProblem(project, inclusion))
        .collect(Collectors.toList());
  }

  private BenefitRiskProblem getNetworkProblem(Project project, NMAInclusionWithResults inclusionWithResults) {
    // output
    URI modelURI = linkService.getModelSourceLink(project, inclusionWithResults.getModel());

    final Map<URI, CriterionEntry> criteria = networkMetaAnalysisService.buildCriteriaForInclusion(inclusionWithResults, modelURI);

    final Map<String, AlternativeEntry> alternatives = networkMetaAnalysisService.buildAlternativesForInclusion(inclusionWithResults);

    DataSourceEntry dataSourceEntry = criteria.values().iterator().next().getDataSources().get(0); // one criterion -> one datasource per NMA
    AbstractMeasurementEntry relativePerformance = networkBenefitRiskPerformanceEntryBuilder.build(inclusionWithResults, dataSourceEntry);
    return new BenefitRiskProblem(criteria, alternatives, Collections.singletonList(relativePerformance));
  }

  private List<BenefitRiskProblem> getSingleStudyProblems(Project project, BenefitRiskAnalysis analysis, Map<Integer, Outcome> outcomesById, Set<AbstractIntervention> includedInterventions) {
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

  private Set<Integer> getIdsFromInclusions(List<BenefitRiskNMAOutcomeInclusion> outcomeInclusions,
                                            Function<BenefitRiskNMAOutcomeInclusion, Integer> idSelector) {
    return outcomeInclusions.stream()
        .map(idSelector)
        .filter(Objects::nonNull)
        .collect(toSet());
  }


  private SingleStudyBenefitRiskProblem getSingleStudyBenefitRiskProblem(Project project, URI studyGraphUri,
                                                                         Set<Outcome> outcomes,
                                                                         Set<AbstractIntervention> includedInterventions) {
    SingleStudyContext context = singleStudyBenefitRiskService.buildContext(project, studyGraphUri, outcomes, includedInterventions);

    TrialDataStudy trialDataStudy = singleStudyBenefitRiskService.getSingleStudyMeasurements(project, studyGraphUri, context);
    List<TrialDataArm> armsWithMatching = singleStudyBenefitRiskService.getArmsWithMatching(includedInterventions, trialDataStudy.getTrialDataArms());

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

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
import org.drugis.addis.problems.model.problemEntry.AbstractProblemEntry;
import org.drugis.addis.problems.service.*;
import org.drugis.addis.problems.service.model.AbstractMeasurementEntry;
import org.drugis.addis.projects.Project;
import org.drugis.addis.projects.repository.ProjectRepository;
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

@Service
public class ProblemServiceImpl implements ProblemService {

  @Inject
  private AnalysisRepository analysisRepository;

  @Inject
  private AnalysisService analysisService;

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
  private NetworkBenefitRiskService networkBenefitRiskService;

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
    List<TrialDataStudy> studies = analysisService.buildEvidenceTable(project.getId(), analysis.getId());
    List<AbstractProblemEntry> entries = networkMetaAnalysisService.buildAbsolutePerformanceEntries(analysis, studies);
    RelativeEffectData relativeEffectData = networkMetaAnalysisService.buildRelativeEffectData(analysis, studies);
    // remove studies without entries from final list
    List<TrialDataStudy> studiesWithEntries = networkMetaAnalysisService.getStudiesWithEntries(studies, entries);

    // add covariate values to problem
    Map<String, Map<String, Double>> studyLevelCovariates = networkMetaAnalysisService.getStudyLevelCovariates(project, analysis, studiesWithEntries);

    return new NetworkMetaAnalysisProblem(entries, relativeEffectData, treatments, studyLevelCovariates);
  }

  private BenefitRiskProblem getBenefitRiskAnalysisProblem(
          Project project, BenefitRiskAnalysis analysis
  ) throws SQLException, IOException, UnexpectedNumberOfResultsException, URISyntaxException, ResourceDoesNotExistException {
    final Map<Integer, Outcome> outcomesById = getOutcomesById(project.getId(), analysis);
    final Set<AbstractIntervention> includedInterventions = analysisService.getIncludedInterventions(analysis);

    List<BenefitRiskStudyOutcomeInclusion> benefitRiskStudyOutcomeInclusions = analysis.getBenefitRiskStudyOutcomeInclusions();
    List<BenefitRiskProblem> allProblems = new ArrayList<>();
    allProblems.addAll(getNetworkProblems(project, analysis, outcomesById, includedInterventions));
    allProblems.addAll(getSingleStudyProblems(project, benefitRiskStudyOutcomeInclusions, outcomesById, includedInterventions));

    Map<URI, CriterionEntry> criteriaWithBaseline = new HashMap<>();
    Map<String, AlternativeEntry> alternativesById = new HashMap<>();
    List<AbstractMeasurementEntry> performanceTable = new ArrayList<>();

    allProblems.forEach(problem -> {
      criteriaWithBaseline.putAll(problem.getCriteria());
      alternativesById.putAll(problem.getAlternatives());
      performanceTable.addAll(problem.getPerformanceTable());
    });
    return new BenefitRiskProblem(WebConstants.SCHEMA_VERSION, criteriaWithBaseline, alternativesById, performanceTable);
  }

  private Map<Integer, Outcome> getOutcomesById(
          Integer projectId,
          BenefitRiskAnalysis analysis
  ) {
    final Set<Integer> outcomeIds = getIdsFromInclusions(
            analysis.getBenefitRiskNMAOutcomeInclusions(),
            BenefitRiskNMAOutcomeInclusion::getOutcomeId
    );
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

  private List<BenefitRiskProblem> getNetworkProblems(
          Project project,
          BenefitRiskAnalysis analysis,
          Map<Integer, Outcome> outcomesById,
          Set<AbstractIntervention> includedInterventions
  ) throws IOException, SQLException, UnexpectedNumberOfResultsException, URISyntaxException {
    if (analysis.getBenefitRiskNMAOutcomeInclusions().size() == 0) {
      return new ArrayList<>();
    }
    final Map<Integer, Model> modelsById = getModelsById(analysis);
    Collection<Model> models = Sets.newHashSet(modelsById.values());
    final Map<Integer, JsonNode> resultsByModelId = networkMetaAnalysisService.getPataviResultsByModelId(models);
    return analysis.getBenefitRiskNMAOutcomeInclusions().stream()
            .filter(inclusion -> networkBenefitRiskService.hasBaseline(inclusion, modelsById, includedInterventions))
            .filter(this::hasModel)
            .filter(inclusion -> networkBenefitRiskService.hasResults(resultsByModelId, inclusion))
            .map(inclusion -> networkBenefitRiskService.getNmaInclusionWithResults(outcomesById, includedInterventions, modelsById, resultsByModelId, inclusion))
            .map(inclusion -> networkBenefitRiskService.getNetworkProblem(project, inclusion))
            .collect(Collectors.toList());
  }

  private boolean hasModel(BenefitRiskNMAOutcomeInclusion inclusion) {
    return inclusion.getModelId() != null;
  }

  private List<BenefitRiskProblem> getSingleStudyProblems(
          Project project,
          List<BenefitRiskStudyOutcomeInclusion> benefitRiskStudyOutcomeInclusions,
          Map<Integer, Outcome> outcomesById,
          Set<AbstractIntervention> includedInterventions
  ) {
    return benefitRiskStudyOutcomeInclusions.stream()
            .map(inclusion -> getBenefitRiskProblemForInclusion(project, outcomesById, includedInterventions, inclusion))
            .collect(toList());
  }

  private SingleStudyBenefitRiskProblem getBenefitRiskProblemForInclusion(Project project, Map<Integer, Outcome> outcomesById, Set<AbstractIntervention> includedInterventions, BenefitRiskStudyOutcomeInclusion inclusion) {
    Outcome outcome = outcomesById.get(inclusion.getOutcomeId());
    return singleStudyBenefitRiskService.getSingleStudyBenefitRiskProblem(
            project, inclusion, outcome, includedInterventions);
  }

  @Override
  public NetworkMetaAnalysisProblem applyModelSettings(
          NetworkMetaAnalysisProblem problem,
          Model model
  ) {
    if (model.getSensitivity() != null && model.getSensitivity().get("omittedStudy") != null) {
      List<AbstractProblemEntry> entries = problem.getEntries();
      String study = (String) model.getSensitivity().get("omittedStudy");
      entries = removeOmittedStudy(entries, study);
      return new NetworkMetaAnalysisProblem(entries, problem.getTreatments(), problem.getStudyLevelCovariates());
    }
    return problem;
  }

  private List<AbstractProblemEntry> removeOmittedStudy(List<AbstractProblemEntry> entries, String study) {
    entries = entries.stream()
            .filter(e -> !Objects.equals(e.getStudy(), study))
            .collect(toList());
    return entries;
  }

  private Set<Integer> getIdsFromInclusions(
          List<BenefitRiskNMAOutcomeInclusion> outcomeInclusions,
          Function<BenefitRiskNMAOutcomeInclusion, Integer> idSelector
  ) {
    return outcomeInclusions.stream()
            .map(idSelector)
            .filter(Objects::nonNull)
            .collect(toSet());
  }
}

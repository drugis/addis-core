package org.drugis.addis.analyses.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import org.drugis.addis.analyses.model.BenefitRiskAnalysis;
import org.drugis.addis.analyses.model.BenefitRiskNMAOutcomeInclusion;
import org.drugis.addis.analyses.model.BenefitRiskStudyOutcomeInclusion;
import org.drugis.addis.analyses.model.InterventionInclusion;
import org.drugis.addis.analyses.repository.BenefitRiskAnalysisRepository;
import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.analyses.service.BenefitRiskAnalysisService;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ProblemCreationException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.interventions.repository.InterventionRepository;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.outcomes.repository.OutcomeRepository;
import org.drugis.addis.problems.model.AbstractProblem;
import org.drugis.addis.problems.service.ProblemService;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.scenarios.repository.ScenarioRepository;
import org.drugis.addis.security.Account;
import org.drugis.addis.subProblems.service.SubProblemService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by connor on 9-3-16.
 */
@Service
public class BenefitRiskAnalysisServiceImpl implements BenefitRiskAnalysisService {

  @Inject
  private AnalysisService analysisService;

  @Inject
  @Lazy
  private BenefitRiskAnalysisRepository benefitRiskAnalysisRepository;

  @Inject
  private OutcomeRepository outcomeRepository;

  @Inject
  private ProblemService problemService;

  @Inject
  private SubProblemService subProblemService;

  @Inject
  private ProjectService projectService;

  @Inject
  private InterventionRepository interventionRepository;

  @Inject
  private ScenarioRepository scenarioRepository;
  private ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public BenefitRiskAnalysis update(Account user, Integer projectId,
                                    BenefitRiskAnalysis analysis, String scenarioState, String path) throws IOException, ResourceDoesNotExistException, MethodNotAllowedException, ProblemCreationException {
    BenefitRiskAnalysis storedAnalysis = benefitRiskAnalysisRepository.find(analysis.getId());
    if (storedAnalysis.isFinalized()) {
      throw new MethodNotAllowedException();
    }
    projectService.checkProjectExistsAndModifiable(user, analysis.getProjectId());
    if (analysis.isFinalized()) {
      AbstractProblem problem = problemService.getProblem(projectId, analysis.getId());
      String problemString = objectMapper.writeValueAsString(problem);
      analysis.setProblem(problemString);
      subProblemService.createMCDADefaults(projectId, analysis.getId(), scenarioState);
    }
    return benefitRiskAnalysisRepository.update(user, analysis);
  }

  @Override
  public void updateBenefitRiskAnalysis(Account user, BenefitRiskAnalysis analysis) throws ResourceDoesNotExistException, MethodNotAllowedException {
    projectService.checkProjectExistsAndModifiable(user, analysis.getProjectId());
    analysisService.checkProjectIdChange(analysis);

    Set<AbstractIntervention> interventions = interventionRepository.query(analysis.getProjectId());
    Map<Integer, AbstractIntervention> interventionMap = interventions.stream()
        .collect(Collectors.toMap(AbstractIntervention::getId, Function.identity()));

    if (!analysis.getInterventionInclusions().isEmpty()) {
      // do not allow selection of interventions that are not in the project
      for (InterventionInclusion interventionInclusion : analysis.getInterventionInclusions()) {
        if (!interventionMap.get(interventionInclusion.getInterventionId()).getProject().equals(analysis.getProjectId())) {
          throw new ResourceDoesNotExistException();
        }
      }
    }
    if (!analysis.getBenefitRiskNMAOutcomeInclusions().isEmpty()) {
      // do not allow selection of outcomes that are not in the project
      for (BenefitRiskNMAOutcomeInclusion benefitRiskNMAOutcomeInclusion : analysis.getBenefitRiskNMAOutcomeInclusions()) {
        Integer outcomeId = benefitRiskNMAOutcomeInclusion.getOutcomeId();
        Outcome outcome = outcomeRepository.get(outcomeId);
        if (!outcome.getProject().equals(analysis.getProjectId())) {
          throw new ResourceDoesNotExistException();
        }
      }
    }
    if (!analysis.getBenefitRiskStudyOutcomeInclusions().isEmpty()) {
      // do not allow selection of outcomes that are not in the project
      for (BenefitRiskStudyOutcomeInclusion benefitRiskStudyOutcomeInclusion : analysis.getBenefitRiskStudyOutcomeInclusions()) {
        Integer outcomeId = benefitRiskStudyOutcomeInclusion.getOutcomeId();
        Outcome outcome = outcomeRepository.get(outcomeId);
        if (!outcome.getProject().equals(analysis.getProjectId())) {
          throw new ResourceDoesNotExistException();
        }
      }
    }
  }

  @Override
  public List<BenefitRiskNMAOutcomeInclusion> removeBaselinesWithoutIntervention(BenefitRiskAnalysis analysis, BenefitRiskAnalysis oldAnalysis) {
    HashSet<InterventionInclusion> newInterventionInclusions = new HashSet<>(analysis.getInterventionInclusions());
    HashSet<InterventionInclusion> oldInterventionInclusions = new HashSet<>(oldAnalysis.getInterventionInclusions());
    if (!newInterventionInclusions.equals(oldInterventionInclusions)) {
      Sets.SetView<InterventionInclusion> difference = Sets.symmetricDifference(Sets.newHashSet(oldAnalysis.getInterventionInclusions()), Sets.newHashSet(analysis.getInterventionInclusions()));
      Integer removedInterventionId = difference.iterator().next().getInterventionId();
      ObjectMapper om = new ObjectMapper();
      return analysis.getBenefitRiskNMAOutcomeInclusions().stream().map(benefitRiskNMAOutcomeInclusion -> {
        if (benefitRiskNMAOutcomeInclusion.getBaseline() != null) {
          try {
            JsonNode baseline = om.readTree(benefitRiskNMAOutcomeInclusion.getBaseline());
            String baselineInterventionName = baseline.get("name").asText();
            AbstractIntervention intervention = interventionRepository.getByProjectIdAndName(analysis.getProjectId(), baselineInterventionName);
            if (intervention.getId().equals(removedInterventionId)) {
              benefitRiskNMAOutcomeInclusion.setBaseline(null);
            }
          } catch (IOException e) {
            throw new RuntimeException("Attempt to read baseline " + benefitRiskNMAOutcomeInclusion.getBaseline());
          }
        }
        return benefitRiskNMAOutcomeInclusion;
      }).collect(Collectors.toList());
    }
    return analysis.getBenefitRiskNMAOutcomeInclusions();
  }
}

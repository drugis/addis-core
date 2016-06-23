package org.drugis.addis.analyses.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.drugis.addis.analyses.InterventionInclusion;
import org.drugis.addis.analyses.MbrOutcomeInclusion;
import org.drugis.addis.analyses.MetaBenefitRiskAnalysis;
import org.drugis.addis.analyses.repository.MetaBenefitRiskAnalysisRepository;
import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.analyses.service.MetaBenefitRiskAnalysisService;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.interventions.repository.InterventionRepository;
import org.drugis.addis.interventions.service.impl.InvalidTypeForDoseCheckException;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.outcomes.repository.OutcomeRepository;
import org.drugis.addis.patavitask.repository.UnexpectedNumberOfResultsException;
import org.drugis.addis.problems.model.AbstractProblem;
import org.drugis.addis.problems.service.ProblemService;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.scenarios.Scenario;
import org.drugis.addis.scenarios.repository.ScenarioRepository;
import org.drugis.addis.security.Account;
import org.drugis.addis.trialverse.service.impl.ReadValueException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

/**
 * Created by connor on 9-3-16.
 */
@Service
public class MetaBenefitRiskAnalysisServiceImpl implements MetaBenefitRiskAnalysisService {

  @Inject
  private AnalysisService analysisService;

  @Inject
  @Lazy
  private MetaBenefitRiskAnalysisRepository metaBenefitRiskAnalysisRepository;

  @Inject
  private OutcomeRepository outcomeRepository;

  @Inject
  private ProblemService problemService;

  @Inject
  ProjectService projectService;

  @Inject
  InterventionRepository interventionRepository;



  @Inject
  private ScenarioRepository scenarioRepository;
  private ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public MetaBenefitRiskAnalysis update(Account user, Integer projectId, MetaBenefitRiskAnalysis analysis) throws URISyntaxException, SQLException, IOException, ResourceDoesNotExistException, MethodNotAllowedException, ReadValueException, InvalidTypeForDoseCheckException, UnexpectedNumberOfResultsException {
    MetaBenefitRiskAnalysis storedAnalysis = metaBenefitRiskAnalysisRepository.find(analysis.getId());
    if(storedAnalysis.isFinalized()) {
      throw new MethodNotAllowedException();
    }
    if(analysis.isFinalized()) {
      // create default scenario
      AbstractProblem problem = problemService.getProblem(projectId, analysis.getId());
      String problemString = objectMapper.writeValueAsString(problem);
      analysis.setProblem(problemString);
      scenarioRepository.create(analysis.getId(), Scenario.DEFAULT_TITLE, "{\"problem\":" + problemString + "}");
    }
    return metaBenefitRiskAnalysisRepository.update(user, analysis);
  }

  @Override
  public void checkMetaBenefitRiskAnalysis(Account user, MetaBenefitRiskAnalysis analysis) throws ResourceDoesNotExistException, MethodNotAllowedException {
    projectService.checkProjectExistsAndModifiable(user, analysis.getProjectId());
    analysisService.checkProjectIdChange(analysis);

    List<AbstractIntervention> interventions = interventionRepository.query(analysis.getProjectId());
    Map<Integer, AbstractIntervention> interventionMap = interventions.stream()
            .collect(Collectors.toMap(AbstractIntervention::getId, Function.identity()));

    if (isNotEmpty(analysis.getInterventionInclusions())) {
      // do not allow selection of interventions that are not in the project
      for (InterventionInclusion interventionInclusion : analysis.getInterventionInclusions()) {
        if (!interventionMap.get(interventionInclusion.getInterventionId()).getProject().equals(analysis.getProjectId())) {
          throw new ResourceDoesNotExistException();
        }
      }
    }
    if (isNotEmpty(analysis.getMbrOutcomeInclusions())) {
      // do not allow selection of outcomes that are not in the project
      for (MbrOutcomeInclusion mbrOutcomeInclusion : analysis.getMbrOutcomeInclusions()) {
        Integer outcomeId = mbrOutcomeInclusion.getOutcomeId();
        Outcome outcome = outcomeRepository.get(outcomeId);
        if (!outcome.getProject().equals(analysis.getProjectId())) {
          throw new ResourceDoesNotExistException();
        }
      }
    }
  }
}

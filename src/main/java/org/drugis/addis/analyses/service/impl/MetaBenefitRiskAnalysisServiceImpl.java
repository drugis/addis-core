package org.drugis.addis.analyses.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.drugis.addis.analyses.MetaBenefitRiskAnalysis;
import org.drugis.addis.analyses.repository.MetaBenefitRiskAnalysisRepository;
import org.drugis.addis.analyses.service.MetaBenefitRiskAnalysisService;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.problems.model.AbstractProblem;
import org.drugis.addis.problems.service.ProblemService;
import org.drugis.addis.scenarios.Scenario;
import org.drugis.addis.scenarios.repository.ScenarioRepository;
import org.drugis.addis.security.Account;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

/**
 * Created by connor on 9-3-16.
 */
@Service
public class MetaBenefitRiskAnalysisServiceImpl implements MetaBenefitRiskAnalysisService {
  @Inject
  private MetaBenefitRiskAnalysisRepository metaBenefitRiskAnalysisRepository;

  @Inject
  private ProblemService problemService;

  @Inject
  private ScenarioRepository scenarioRepository;
  private ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public MetaBenefitRiskAnalysis update(Account user, Integer projectId, MetaBenefitRiskAnalysis analysis) throws URISyntaxException, SQLException, IOException, ResourceDoesNotExistException, MethodNotAllowedException {
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
}

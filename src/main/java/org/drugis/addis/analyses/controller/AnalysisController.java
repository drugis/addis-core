package org.drugis.addis.analyses.controller;

import org.apache.http.HttpStatus;
import org.drugis.addis.analyses.*;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.analyses.repository.MetaBenefitRiskAnalysisRepository;
import org.drugis.addis.analyses.repository.NetworkMetaAnalysisRepository;
import org.drugis.addis.analyses.repository.SingleStudyBenefitRiskAnalysisRepository;
import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.base.AbstractAddisCoreController;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.scenarios.Scenario;
import org.drugis.addis.scenarios.repository.ScenarioRepository;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * Created by connor on 3/11/14.
 */
@Controller
@Transactional("ptmAddisCore")
public class AnalysisController extends AbstractAddisCoreController {

  final static Logger logger = LoggerFactory.getLogger(AnalysisController.class);

  @Inject
  AnalysisRepository analysisRepository;
  @Inject
  SingleStudyBenefitRiskAnalysisRepository singleStudyBenefitRiskAnalysisRepository;
  @Inject
  NetworkMetaAnalysisRepository networkMetaAnalysisRepository;
  @Inject
  MetaBenefitRiskAnalysisRepository metaBenefitRiskAnalysisRepository;
  @Inject
  AccountRepository accountRepository;
  @Inject
  AnalysisService analysisService;
  @Inject
  private ScenarioRepository scenarioRepository;
  @Inject
  private ProjectService projectService;



  @RequestMapping(value = "/projects/{projectId}/analyses", method = RequestMethod.GET, params = {"outcomeIds"})
  @ResponseBody
  public NetworkMetaAnalysis[] queryNetworkMetaAnalysisByOutcomes(Principal currentUser, @PathVariable Integer projectId, @RequestParam(name = "outcomeIds", required=false) List<Integer> outcomeIds) throws MethodNotAllowedException, ResourceDoesNotExistException {
    Account user = accountRepository.findAccountByUsername(currentUser.getName());
    if (user != null) {
      Collection<NetworkMetaAnalysis> networkMetaAnalyses = networkMetaAnalysisRepository.queryByOutcomes(projectId, outcomeIds);
      NetworkMetaAnalysis[] networkMetaAnalysesArray = new NetworkMetaAnalysis[networkMetaAnalyses.size()];
      return networkMetaAnalyses.toArray(networkMetaAnalysesArray);
    } else {
      throw new MethodNotAllowedException();
    }
  }


  @RequestMapping(value = "/projects/{projectId}/analyses", method = RequestMethod.GET)
  @ResponseBody
  public AbstractAnalysis[] query(Principal currentUser, @PathVariable Integer projectId) throws MethodNotAllowedException, ResourceDoesNotExistException {
    Account user = accountRepository.findAccountByUsername(currentUser.getName());
    if (user != null) {
      List<AbstractAnalysis> abstractAnalysisList = analysisRepository.query(projectId);
      AbstractAnalysis[] abstractAnalysesArray = new AbstractAnalysis[abstractAnalysisList.size()];
      return abstractAnalysisList.toArray(abstractAnalysesArray);
    } else {
      throw new MethodNotAllowedException();
    }
  }

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}", method = RequestMethod.GET)
  @ResponseBody
  public AbstractAnalysis get(Principal currentUser, @PathVariable Integer analysisId) throws MethodNotAllowedException, ResourceDoesNotExistException {
    Account user = accountRepository.findAccountByUsername(currentUser.getName());
    if (user != null) {
      return analysisRepository.get(analysisId);
    } else {
      throw new MethodNotAllowedException();
    }
  }


  @RequestMapping(value = "/projects/{projectId}/analyses", method = RequestMethod.POST)
  @ResponseBody
  public AbstractAnalysis create(HttpServletRequest request, HttpServletResponse response, Principal currentUser, @RequestBody AnalysisCommand analysisCommand) throws MethodNotAllowedException, ResourceDoesNotExistException {
    Account user = accountRepository.findAccountByUsername(currentUser.getName());
    if (user != null) {
      AbstractAnalysis analysis;
      switch (analysisCommand.getType()) {
        case AnalysisType.SINGLE_STUDY_BENEFIT_RISK_LABEL:
          analysis = analysisService.createSingleStudyBenefitRiskAnalysis(user, analysisCommand);
          break;
        case AnalysisType.NETWORK_META_ANALYSIS_LABEL:
          analysis = analysisService.createNetworkMetaAnalysis(user, analysisCommand);
          break;
        case AnalysisType.META_BENEFIT_RISK_ANALYSIS_LABEL:
          analysis = metaBenefitRiskAnalysisRepository.create(user, analysisCommand);
          break;
        default:
          throw new RuntimeException("unknown analysis type.");
      }
      response.setStatus(HttpServletResponse.SC_CREATED);
      response.setHeader("Location", request.getRequestURL() + "/");
      return analysis;
    } else {
      throw new MethodNotAllowedException();
    }
  }

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}/setPrimaryModel", method = RequestMethod.POST)
  public void setPrimaryModel(HttpServletResponse response, Principal currentUser,
                              @PathVariable Integer projectId,
                              @PathVariable Integer analysisId,
                              @RequestParam(required=false) Integer modelId) throws MethodNotAllowedException, ResourceDoesNotExistException {
    projectService.checkOwnership(projectId, currentUser);
    analysisRepository.setPrimaryModel(analysisId, modelId);
    response.setStatus(HttpStatus.SC_OK);
  }

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}", method = RequestMethod.POST)
  @ResponseBody
  public AbstractAnalysis update(Principal currentUser, @RequestBody AbstractAnalysis analysis) throws MethodNotAllowedException, ResourceDoesNotExistException, SQLException {
    Account user = accountRepository.findAccountByUsername(currentUser.getName());
    if (user != null) {
      if (analysis instanceof SingleStudyBenefitRiskAnalysis) {
        SingleStudyBenefitRiskAnalysis singleStudyBenefitRiskAnalysis = (SingleStudyBenefitRiskAnalysis) analysis;
        return updateSingleStudyBenefitRiskAnalysis(user, singleStudyBenefitRiskAnalysis);
      } else if (analysis instanceof NetworkMetaAnalysis) {
        return analysisService.updateNetworkMetaAnalysis(user, (NetworkMetaAnalysis) analysis);
      } else if (analysis instanceof MetaBenefitRiskAnalysis) {
        return metaBenefitRiskAnalysisRepository.update(user, (MetaBenefitRiskAnalysis) analysis);
      }
      throw new ResourceDoesNotExistException();
    } else {
      throw new MethodNotAllowedException();
    }
  }

  public SingleStudyBenefitRiskAnalysis updateSingleStudyBenefitRiskAnalysis(Account user, SingleStudyBenefitRiskAnalysis analysis) throws MethodNotAllowedException, ResourceDoesNotExistException {
    SingleStudyBenefitRiskAnalysis oldAnalysis = (SingleStudyBenefitRiskAnalysis) analysisRepository.get(analysis.getId());
    if (oldAnalysis.getProblem() != null) {
      throw new MethodNotAllowedException();
    }
    SingleStudyBenefitRiskAnalysis updatedAnalysis = singleStudyBenefitRiskAnalysisRepository.update(user, analysis);
    if (analysis.getProblem() != null) {
      String state = analysis.getProblem();
      // problem wrapping in state necessary for mcda-web
      scenarioRepository.create(analysis.getId(), Scenario.DEFAULT_TITLE, "{\"problem\":" + state + "}");
    }
    return updatedAnalysis;
  }


}

package org.drugis.addis.analyses.controller;

import org.apache.http.HttpStatus;
import org.drugis.addis.analyses.model.*;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.analyses.repository.NetworkMetaAnalysisRepository;
import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.analyses.service.BenefitRiskAnalysisService;
import org.drugis.addis.base.AbstractAddisCoreController;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ProblemCreationException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.service.impl.InvalidTypeForDoseCheckException;
import org.drugis.addis.patavitask.repository.UnexpectedNumberOfResultsException;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.subProblems.service.SubProblemService;
import org.drugis.addis.trialverse.model.trialdata.TrialDataStudy;
import org.drugis.addis.trialverse.service.impl.ReadValueException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
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
  private AnalysisRepository analysisRepository;
  @Inject
  private NetworkMetaAnalysisRepository networkMetaAnalysisRepository;
  @Inject
  private AccountRepository accountRepository;
  @Inject
  private AnalysisService analysisService;
  @Inject
  private SubProblemService subProblemService;
  @Inject
  private ProjectService projectService;
  @Inject
  private BenefitRiskAnalysisService benefitRiskAnalysisService;

  @RequestMapping(value = "/projects/{projectId}/analyses", method = RequestMethod.GET, params = {"outcomeIds"})
  @ResponseBody
  public NetworkMetaAnalysis[] queryNetworkMetaAnalysisByOutcomes(@PathVariable Integer projectId, @RequestParam(name = "outcomeIds", required = false) List<Integer> outcomeIds) throws MethodNotAllowedException, ResourceDoesNotExistException {
    Collection<NetworkMetaAnalysis> networkMetaAnalyses = networkMetaAnalysisRepository.queryByOutcomes(projectId, outcomeIds);
    NetworkMetaAnalysis[] networkMetaAnalysesArray = new NetworkMetaAnalysis[networkMetaAnalyses.size()];
    return networkMetaAnalyses.toArray(networkMetaAnalysesArray);
  }


  @RequestMapping(value = "/projects/{projectId}/analyses", method = RequestMethod.GET)
  @ResponseBody
  public AbstractAnalysis[] query(@PathVariable Integer projectId) throws MethodNotAllowedException, ResourceDoesNotExistException {
    List<AbstractAnalysis> abstractAnalysisList = analysisRepository.query(projectId);
    AbstractAnalysis[] abstractAnalysesArray = new AbstractAnalysis[abstractAnalysisList.size()];
    return abstractAnalysisList.toArray(abstractAnalysesArray);
  }

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}", method = RequestMethod.GET)
  @ResponseBody
  public AbstractAnalysis get(@PathVariable Integer analysisId) throws MethodNotAllowedException, ResourceDoesNotExistException {
    return analysisRepository.get(analysisId);
  }


  @RequestMapping(value = "/projects/{projectId}/analyses", method = RequestMethod.POST)
  @ResponseBody
  public AbstractAnalysis create(HttpServletRequest request, HttpServletResponse response, Principal currentUser,
                                 @RequestBody AnalysisCommand analysisCommand) throws MethodNotAllowedException, ResourceDoesNotExistException, SQLException, IOException {
    Account user = accountRepository.findAccountByUsername(currentUser.getName());
    if (user != null) {
      AbstractAnalysis analysis;
      switch (analysisCommand.getType()) {
        case AnalysisType.EVIDENCE_SYNTHESIS:
          analysis = analysisService.createNetworkMetaAnalysis(user, analysisCommand);
          break;
        case AnalysisType.BENEFIT_RISK_ANALYSIS_LABEL:
          analysis = analysisService.createBenefitRiskAnalysis(user, analysisCommand);
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
                              @RequestParam(value = "modelId", required = false) Integer modelId) throws MethodNotAllowedException, ResourceDoesNotExistException {
    projectService.checkOwnership(projectId, currentUser);
    networkMetaAnalysisRepository.setPrimaryModel(analysisId, modelId);
    response.setStatus(HttpStatus.SC_OK);
  }

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}", method = RequestMethod.POST)
  @ResponseBody
  public AbstractAnalysis update(Principal currentUser, @PathVariable Integer projectId,
                                 @RequestBody AnalysisUpdateCommand analysisUpdateCommand, HttpServletRequest request) throws MethodNotAllowedException, ResourceDoesNotExistException, SQLException, IOException, URISyntaxException, ReadValueException, InvalidTypeForDoseCheckException, UnexpectedNumberOfResultsException, ProblemCreationException {
    AbstractAnalysis analysis = analysisUpdateCommand.getAnalysis();
    Account user = accountRepository.findAccountByUsername(currentUser.getName());
    if (user != null) {
      if (analysis instanceof NetworkMetaAnalysis) {
        return analysisService.updateNetworkMetaAnalysis(user, (NetworkMetaAnalysis) analysis);
      } else if (analysis instanceof BenefitRiskAnalysis) {
        return benefitRiskAnalysisService.update(user, projectId, (BenefitRiskAnalysis) analysis,
                analysisUpdateCommand.getScenarioState(), request.getRequestURL().toString());
      }
      throw new ResourceDoesNotExistException();
    } else {
      throw new MethodNotAllowedException();
    }
  }

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}/evidenceTable", method = RequestMethod.GET)
  @ResponseBody
  public List<TrialDataStudy> getEvidenceTable(@PathVariable Integer projectId, @PathVariable Integer analysisId) throws ResourceDoesNotExistException, ReadValueException, URISyntaxException, IOException {
    return analysisService.buildEvidenceTable(projectId, analysisId);
  }

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}/setArchivedStatus", method = RequestMethod.POST)
  @ResponseBody
  public void setArchivedStatus(Principal principal, @PathVariable Integer projectId, @PathVariable Integer analysisId, @RequestBody AnalysisArchiveCommand archiveCommand) throws ResourceDoesNotExistException, MethodNotAllowedException {
    projectService.checkOwnership(projectId, principal);
    analysisRepository.setArchived(analysisId, archiveCommand.getIsArchived());
  }

}

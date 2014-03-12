package org.drugis.addis.analyses.controller;

import org.drugis.addis.analyses.Analysis;
import org.drugis.addis.analyses.AnalysisCommand;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.base.AbstractAddisCoreController;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.Collection;

/**
 * Created by connor on 3/11/14.
 */
@Controller
@Transactional("ptmAddisCore")
public class AnalysisController extends AbstractAddisCoreController {

  @Inject
  AnalysisRepository analysisRepository;
  @Inject
  AccountRepository accountRepository;

  @RequestMapping(value = "/projects/{projectId}/analyses", method = RequestMethod.GET)
  @ResponseBody
  public Collection<Analysis> query(Principal currentUser, @PathVariable Integer projectId) throws MethodNotAllowedException, ResourceDoesNotExistException {
    Account user = accountRepository.findAccountByUsername(currentUser.getName());
    if (user != null) {
      return analysisRepository.query(projectId);
    } else {
      throw new MethodNotAllowedException();
    }
  }

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}", method = RequestMethod.GET)
  @ResponseBody
  public Analysis get(Principal currentUser, @PathVariable Integer projectId, @PathVariable Integer analysisId) throws MethodNotAllowedException, ResourceDoesNotExistException {
    Account user = accountRepository.findAccountByUsername(currentUser.getName());
    if (user != null) {
      return analysisRepository.get(projectId, analysisId);
    } else {
      throw new MethodNotAllowedException();
    }
  }


  @RequestMapping(value = "/projects/{projectId}/analyses", method = RequestMethod.POST)
  @ResponseBody
  public Analysis create(HttpServletRequest request, HttpServletResponse response, Principal currentUser, @PathVariable Integer projectId, @RequestBody AnalysisCommand analysisCommand) throws MethodNotAllowedException, ResourceDoesNotExistException {
    Account user = accountRepository.findAccountByUsername(currentUser.getName());
    if (user != null) {
      Analysis analysis = analysisRepository.create(user, analysisCommand);
      response.setStatus(HttpServletResponse.SC_CREATED);
      response.setHeader("Location", request.getRequestURL() + "/");
      return analysis;
    } else {
      throw new MethodNotAllowedException();
    }
  }

}

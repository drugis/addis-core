package org.drugis.addis.outcomes.controller;

import org.drugis.addis.base.AbstractAddisCoreController;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.outcomes.OutcomeCommand;
import org.drugis.addis.projects.repository.ProjectRepository;
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
 * Created by daan on 3/5/14.
 */
@Controller
@Transactional("ptmAddisCore")
public class OutcomeController extends AbstractAddisCoreController {

  @Inject
  private ProjectRepository projectRepository;
  @Inject
  private AccountRepository accountRepository;

  @RequestMapping(value = "/projects/{projectId}/outcomes", method = RequestMethod.GET)
  @ResponseBody
  public Collection<Outcome> query(Principal currentUser, @PathVariable Integer projectId) throws MethodNotAllowedException, ResourceDoesNotExistException {
    Account user = accountRepository.findAccountByUsername(currentUser.getName());
    if (user != null) {
      return projectRepository.getProjectById(projectId).getOutcomes();
    } else {
      throw new MethodNotAllowedException();
    }
  }

  @RequestMapping(value = "/projects/{projectId}/outcomes/{outcomeId}", method = RequestMethod.GET)
  @ResponseBody
  public Outcome get(Principal currentUser, @PathVariable Integer projectId, @PathVariable Integer outcomeId) throws MethodNotAllowedException, ResourceDoesNotExistException {
    Account user = accountRepository.findAccountByUsername(currentUser.getName());
    if (user != null) {
      return projectRepository.getProjectOutcome(projectId, outcomeId);
    } else {
      throw new MethodNotAllowedException();
    }
  }

  @RequestMapping(value = "/projects/{projectId}/outcomes", method = RequestMethod.POST)
  @ResponseBody
  public Outcome create(HttpServletRequest request, HttpServletResponse response, Principal currentUser, @PathVariable Integer projectId, @RequestBody OutcomeCommand outcomeCommand) throws MethodNotAllowedException, ResourceDoesNotExistException {
    Account user = accountRepository.findAccountByUsername(currentUser.getName());
    if (user != null) {
      Outcome outcome = projectRepository.createOutcome(user, projectId, outcomeCommand);
      response.setStatus(HttpServletResponse.SC_CREATED);
      response.setHeader("Location", request.getRequestURL() + "/");
      return outcome;
    } else {
      throw new MethodNotAllowedException();
    }
  }

}

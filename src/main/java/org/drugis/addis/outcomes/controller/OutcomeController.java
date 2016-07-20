package org.drugis.addis.outcomes.controller;

import org.drugis.addis.base.AbstractAddisCoreController;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.outcomes.OutcomeCommand;
import org.drugis.addis.outcomes.controller.command.EditOutcomeCommand;
import org.drugis.addis.outcomes.repository.OutcomeRepository;
import org.drugis.addis.outcomes.service.OutcomeService;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.util.WebConstants;
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
  private AccountRepository accountRepository;
  @Inject
  private OutcomeRepository outcomeRepository;
  @Inject
  private OutcomeService outcomeService;
  @Inject
  private ProjectService projectService;


  @RequestMapping(value = "/projects/{projectId}/outcomes", method = RequestMethod.GET)
  @ResponseBody
  public Collection<Outcome> query(@PathVariable Integer projectId) throws MethodNotAllowedException, ResourceDoesNotExistException {
      return outcomeRepository.query(projectId);
  }

  @RequestMapping(value = "/projects/{projectId}/outcomes/{outcomeId}", method = RequestMethod.GET)
  @ResponseBody
  public Outcome get(@PathVariable Integer projectId, @PathVariable Integer outcomeId) throws MethodNotAllowedException, ResourceDoesNotExistException {
    return outcomeRepository.get(projectId, outcomeId);
  }

  @RequestMapping(value = "/projects/{projectId}/outcomes", method = RequestMethod.POST, produces = WebConstants.APPLICATION_JSON_UTF8_VALUE)
  @ResponseBody
  public Outcome create(HttpServletRequest request, HttpServletResponse response, Principal currentUser, @PathVariable Integer projectId, @RequestBody OutcomeCommand outcomeCommand) throws MethodNotAllowedException, ResourceDoesNotExistException {
    Account user = accountRepository.findAccountByUsername(currentUser.getName());
    if (user != null) {
      Outcome outcome = outcomeRepository.create(user, outcomeCommand);
      response.setStatus(HttpServletResponse.SC_CREATED);
      response.setHeader("Location", request.getRequestURL() + "/");
      return outcome;
    } else {
      throw new MethodNotAllowedException();
    }
  }

  @RequestMapping(value = "/projects/{projectId}/outcomes/{outcomeId}", method = RequestMethod.POST)
  @ResponseBody
  public Outcome edit(Principal currentUser, @PathVariable Integer outcomeId,  @PathVariable Integer projectId, @RequestBody EditOutcomeCommand command) throws Exception {
    Account user = accountRepository.getAccount(currentUser);
    projectService.checkProjectExistsAndModifiable(user, projectId);
    return outcomeService.updateNameAndMotivation(projectId, outcomeId, command.getName(), command.getMotivation());
  }

}

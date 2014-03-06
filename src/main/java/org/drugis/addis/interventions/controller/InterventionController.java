package org.drugis.addis.interventions.controller;

import org.drugis.addis.base.AbstractAddisCoreController;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.Intervention;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.security.Principal;
import java.util.Collection;

/**
 * Created by daan on 3/6/14.
 */
@Controller
@Transactional("ptmAddisCore")
public class InterventionController extends AbstractAddisCoreController {

  @Inject
  private ProjectRepository projectRepository;
  @Inject
  private AccountRepository accountRepository;

  @RequestMapping(value = "/projects/{projectId}/interventions", method = RequestMethod.GET)
  @ResponseBody
  public Collection<Intervention> query(Principal currentUser, @PathVariable Integer projectId) throws MethodNotAllowedException, ResourceDoesNotExistException {
    Account user = accountRepository.findAccountByUsername(currentUser.getName());
    if (user != null) {
      return projectRepository.getProjectById(projectId).getInterventions();
    } else {
      throw new MethodNotAllowedException();
    }
  }


}

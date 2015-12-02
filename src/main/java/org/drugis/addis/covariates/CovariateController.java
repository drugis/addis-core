package org.drugis.addis.covariates;

import org.drugis.addis.base.AbstractAddisCoreController;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
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
 * Created by connor on 12/1/15.
 */
@Controller
public class CovariateController extends AbstractAddisCoreController {

  @Inject
  ProjectService projectService;

  @Inject
  AccountRepository accountRepository;

  @Inject
  CovariateRepository covariateRepository;

  @RequestMapping(value = "/projects/{projectId}/covariates", method = RequestMethod.GET)
  @ResponseBody
  public Collection<Covariate> getCovariatesForProject(@PathVariable Integer projectId) {
    return covariateRepository.findByProject(projectId);
  }

  @RequestMapping(value = "/projects/{projectId}/covariates", method = RequestMethod.POST)
  @ResponseBody
  @Transactional("ptmAddisCore")
  public Covariate addCovariateToProject(HttpServletRequest request, HttpServletResponse response, Principal currentUser,
                                         @PathVariable Integer projectId, @RequestBody AddCovariateCommand command)
          throws MethodNotAllowedException, ResourceDoesNotExistException {
    Account user = accountRepository.findAccountByUsername(currentUser.getName());
    if (user != null) {
      projectService.checkProjectExistsAndModifiable(user, projectId);
      Covariate covariate = covariateRepository.createForProject(projectId, command.getDefinition(), command.getName(), command.getMotivation());
      response.setStatus(HttpServletResponse.SC_CREATED);
      response.setContentType(WebConstants.APPLICATION_JSON_UTF8.toString());
      response.setHeader("Location", request.getRequestURL() + "/");
      return covariate;
    } else {
      throw new MethodNotAllowedException();
    }
  }
}

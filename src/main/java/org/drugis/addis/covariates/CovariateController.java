package org.drugis.addis.covariates;

import org.apache.http.HttpStatus;
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
@Transactional("ptmAddisCore")
public class CovariateController extends AbstractAddisCoreController {

  @Inject
  ProjectService projectService;

  @Inject
  AccountRepository accountRepository;

  @Inject
  CovariateRepository covariateRepository;

  @Inject
  CovariateService covariateService;

  @RequestMapping(value = "/projects/{projectId}/covariates", method = RequestMethod.GET)
  @ResponseBody
  public Collection<Covariate> getCovariatesForProject(@PathVariable(value="projectId") Integer projectId) {
    return covariateRepository.findByProject(projectId);
  }

  @RequestMapping(value = "/projects/{projectId}/covariates", method = RequestMethod.POST)
  @ResponseBody
  public Covariate addCovariateToProject(HttpServletRequest request, HttpServletResponse response, Principal currentUser,
                                         @PathVariable(value="projectId") Integer projectId, @RequestBody AddCovariateCommand command)
          throws MethodNotAllowedException, ResourceDoesNotExistException {
    Account user = accountRepository.findAccountByUsername(currentUser.getName());
    if (user != null) {
      projectService.checkProjectExistsAndModifiable(user, projectId);
      Covariate covariate = covariateRepository.createForProject(projectId,
              command.getCovariateDefinitionKey(), command.getName(), command.getMotivation(), command.getType());
      response.setStatus(HttpServletResponse.SC_CREATED);
      response.setContentType(WebConstants.getApplicationJsonUtf8Value());
      response.setHeader("Location", request.getRequestURL() + "/");
      return covariate;
    } else {
      throw new MethodNotAllowedException();
    }
  }

  @RequestMapping(value = "/projects/{projectId}/covariates/{covariateId}", method = RequestMethod.DELETE)
  public void deleteCovariate(Principal currentUser, HttpServletResponse response, @PathVariable(value="projectId") Integer projectId,
                              @PathVariable Integer covariateId) throws ResourceDoesNotExistException, MethodNotAllowedException {
    Account user = accountRepository.findAccountByUsername(currentUser.getName());
    covariateService.delete(user, projectId, covariateId);
    response.setStatus(HttpStatus.SC_OK);
  }

}

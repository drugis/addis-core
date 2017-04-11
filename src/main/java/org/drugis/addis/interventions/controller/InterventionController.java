package org.drugis.addis.interventions.controller;

import org.apache.http.HttpStatus;
import org.drugis.addis.base.AbstractAddisCoreController;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.SetMultipliersCommand;
import org.drugis.addis.interventions.controller.command.AbstractInterventionCommand;
import org.drugis.addis.interventions.controller.command.EditInterventionCommand;
import org.drugis.addis.interventions.controller.viewAdapter.AbstractInterventionViewAdapter;
import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.interventions.model.InvalidConstraintException;
import org.drugis.addis.interventions.repository.InterventionRepository;
import org.drugis.addis.interventions.service.InterventionService;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by daan on 3/5/14.
 */
@Controller
@Transactional("ptmAddisCore")
public class InterventionController extends AbstractAddisCoreController {

  @Inject
  private AccountRepository accountRepository;
  @Inject
  private InterventionRepository interventionRepository;
  @Inject
  private InterventionService interventionService;
  @Inject
  private ProjectService projectService;


  @RequestMapping(value = "/projects/{projectId}/interventions", method = RequestMethod.GET)
  @ResponseBody
  public List<AbstractInterventionViewAdapter> query(@PathVariable Integer projectId) throws MethodNotAllowedException, ResourceDoesNotExistException {
    Set<AbstractIntervention> interventions = interventionRepository.query(projectId);
    List<AbstractInterventionViewAdapter> viewAdapters = interventions.stream().map(AbstractIntervention::toViewAdapter).collect(Collectors.toList());
    return viewAdapters;
  }

  @RequestMapping(value = "/projects/{projectId}/interventions/{interventionId}", method = RequestMethod.GET)
  @ResponseBody
  public AbstractInterventionViewAdapter get(@PathVariable Integer projectId, @PathVariable Integer interventionId) throws MethodNotAllowedException, ResourceDoesNotExistException {
    AbstractIntervention intervention = interventionRepository.get(interventionId);
    if (!intervention.getProject().equals(projectId)) {
      throw new ResourceDoesNotExistException();
    }
    return intervention.toViewAdapter();
  }

  @RequestMapping(value = "/projects/{projectId}/interventions/{interventionId}", method = RequestMethod.POST)
  @ResponseBody
  public AbstractInterventionViewAdapter edit(Principal currentUser, @PathVariable Integer projectId,
                                              @PathVariable Integer interventionId,
                                              @RequestBody EditInterventionCommand command) throws Exception {
    Account user = accountRepository.getAccount(currentUser);
    projectService.checkProjectExistsAndModifiable(user, projectId);
    AbstractIntervention updatedIntervention = interventionService.updateNameAndMotivation(projectId, interventionId, command.getName(), command.getMotivation());
    return updatedIntervention.toViewAdapter();
  }

  @RequestMapping(value = "/projects/{projectId}/interventions", method = RequestMethod.POST)
  @ResponseBody
  public AbstractInterventionViewAdapter create(HttpServletRequest request, HttpServletResponse response, Principal currentUser, @PathVariable Integer projectId,
                                                @RequestBody AbstractInterventionCommand interventionCommand) throws MethodNotAllowedException, ResourceDoesNotExistException, InvalidConstraintException {
    Account user = accountRepository.findAccountByUsername(currentUser.getName());
    projectService.checkProjectExistsAndModifiable(user, projectId);
    AbstractIntervention intervention = interventionRepository.create(user, interventionCommand);
    response.setStatus(HttpServletResponse.SC_CREATED);
    response.setHeader("Location", request.getRequestURL() + "/");
    return intervention.toViewAdapter();
  }

  @RequestMapping(value = "/projects/{projectId}/interventions/{interventionId}", method = RequestMethod.DELETE)
  public void deleteIntervention(@PathVariable Integer projectId, @PathVariable Integer interventionId,
                                 Principal currentUser, HttpServletResponse response) throws ResourceDoesNotExistException, MethodNotAllowedException {
    Account user = accountRepository.findAccountByUsername(currentUser.getName());
    projectService.checkProjectExistsAndModifiable(user, projectId);
    interventionService.delete(projectId, interventionId);
    response.setStatus(HttpStatus.SC_OK);
  }

  @RequestMapping(value = "/projects/{projectId}/interventions/{interventionId}/setConversionMultiplier", method = RequestMethod.POST)
  public void setConversionMultiplier(@PathVariable Integer projectId, @PathVariable Integer interventionId,
                                      Principal currentUser, HttpServletResponse response,
                                      @RequestBody SetMultipliersCommand command) throws ResourceDoesNotExistException, MethodNotAllowedException {
    Account user = accountRepository.findAccountByUsername(currentUser.getName());
    projectService.checkProjectExistsAndModifiable(user, projectId);
    interventionService.setMultipliers(interventionId, command);
  }

}

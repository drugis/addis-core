package org.drugis.addis.projects.controller;

import org.apache.http.HttpStatus;
import org.drugis.addis.base.AbstractAddisCoreController;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.projects.CopyCommand;
import org.drugis.addis.projects.Project;
import org.drugis.addis.projects.ProjectCommand;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.projects.repository.ReportRepository;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.projects.service.impl.UpdateProjectException;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.trialverse.model.trialdata.TrialDataStudy;
import org.drugis.addis.trialverse.service.impl.ReadValueException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
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
 * Created by daan on 2/6/14.
 */
@Controller
@Transactional("ptmAddisCore")
public class ProjectController extends AbstractAddisCoreController {

  final static Logger logger = LoggerFactory.getLogger(ProjectController.class);
  final static String DEFAULT_REPORT_TEXT
          = "default report text";
  @Inject
  private ProjectRepository projectsRepository;

  @Inject
  private ProjectService projectService;

  @Inject
  private AccountRepository accountRepository;

  @Inject
  private ReportRepository reportRepository;

  @RequestMapping(value = "/projects", method = RequestMethod.GET)
  @ResponseBody
  public Collection<Project> query(@RequestParam(required = false) Integer owner) {
    return owner == null ? projectsRepository.query() : projectsRepository.queryByOwnerId(owner);
  }

  @RequestMapping(value = "/projects/{projectId}", method = RequestMethod.GET)
  @ResponseBody
  public Project get(@PathVariable Integer projectId) throws ResourceDoesNotExistException {
    return projectsRepository.get(projectId);
  }

  @RequestMapping(value = "/projects/{projectId}", method = RequestMethod.POST)
  @ResponseBody
  public Project update(@PathVariable Integer projectId, @RequestBody EditProjectCommand command, Principal currentUser) throws ResourceDoesNotExistException, MethodNotAllowedException, UpdateProjectException {
    projectService.checkOwnership(projectId, currentUser);
    return projectService.updateProject(projectId, command.getName(), command.getDescription());
  }

  @RequestMapping(value = "/projects/{projectId}/studies", method = RequestMethod.GET)
  @ResponseBody
  public List<TrialDataStudy> queryStudies(@PathVariable Integer projectId) throws URISyntaxException, ResourceDoesNotExistException, ReadValueException, IOException {
    return projectService.queryMatchedStudies(projectId);
  }

  @RequestMapping(value = "/projects", method = RequestMethod.POST)
  @ResponseBody
  public Project create(HttpServletRequest request, HttpServletResponse response, Principal currentUser, @RequestBody ProjectCommand command) {
    Account user = accountRepository.findAccountByUsername(currentUser.getName());
    Project Project = projectsRepository.create(user, command);
    response.setStatus(HttpServletResponse.SC_CREATED);
    response.setHeader("Location", request.getRequestURL() + "/");
    return Project;
  }

  @RequestMapping(value = "/projects/{projectId}/report", produces = "text/plain", method = RequestMethod.GET)
  @ResponseBody
  public String getReport(@PathVariable Integer projectId) {
    try {
      return reportRepository.get(projectId);
    } catch (EmptyResultDataAccessException e) {
      return DEFAULT_REPORT_TEXT;
    }
  }

  @RequestMapping(value = "/projects/{projectId}/report", method = RequestMethod.PUT)
  public void updateReport(@PathVariable Integer projectId, @RequestBody String newReport,
                           HttpServletResponse response,
                           Principal currentUser) throws ResourceDoesNotExistException, MethodNotAllowedException {
    projectService.checkOwnership(projectId, currentUser);
    reportRepository.update(projectId, newReport);
    response.setStatus(HttpStatus.SC_OK);
  }

  @RequestMapping(value = "/projects/{projectId}/setArchivedStatus", method = RequestMethod.POST)
  @ResponseBody
  public void setArchivedStatus(Principal principal, @PathVariable Integer projectId, @RequestBody ProjectArchiveCommand archiveCommand) throws ResourceDoesNotExistException, MethodNotAllowedException {
    projectService.checkOwnership(projectId, principal);
    projectsRepository.setArchived(projectId, archiveCommand.getIsArchived());
  }

  @RequestMapping(value = "/projects/{projectId}/copy", method = RequestMethod.POST)
  @ResponseBody
  public Integer copy(Principal principal, @PathVariable Integer projectId,
                      @RequestBody CopyCommand copyCommand) throws ResourceDoesNotExistException,
          MethodNotAllowedException, SQLException {
    Account user = accountRepository.findAccountByUsername(principal.getName());
    return projectService.copy(user, projectId, copyCommand.getNewTitle());
  }

  @RequestMapping(value = "/projects/{projectId}/update", method = RequestMethod.POST)
  @ResponseBody
  public Integer update(Principal principal, @PathVariable Integer projectId) throws ResourceDoesNotExistException, MethodNotAllowedException, ReadValueException, URISyntaxException, SQLException, IOException {
    Account user = accountRepository.findAccountByUsername(principal.getName());
    return projectService.createUpdated(user, projectId);
  }
}

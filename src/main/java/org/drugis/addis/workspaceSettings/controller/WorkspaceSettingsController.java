package org.drugis.addis.workspaceSettings.controller;

import org.drugis.addis.base.AbstractAddisCoreController;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.workspaceSettings.repository.WorkspaceSettingsRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.security.Principal;

@Controller
public class WorkspaceSettingsController extends AbstractAddisCoreController {

  @Inject
  private WorkspaceSettingsRepository workspaceSettingsRepository;

  @Inject
  private ProjectService projectService;

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}/workspaceSettings", method = RequestMethod.GET)
  @ResponseBody
  public String get(@PathVariable(value="analysisId") Integer analysisId) {
    return workspaceSettingsRepository.get(analysisId).getSettings();
  }

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}/workspaceSettings", method = RequestMethod.PUT)
  @ResponseBody
  public void put(Principal principal, @PathVariable(value="projectId") Integer projectId, @PathVariable(value="analysisId") Integer analysisId,
                  @RequestBody String settings) throws ResourceDoesNotExistException, MethodNotAllowedException {
    projectService.checkOwnership(projectId, principal);
    workspaceSettingsRepository.put(analysisId, settings);
  }
}

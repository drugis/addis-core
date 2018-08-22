package org.drugis.addis.workspaceSettings.controller;

import org.drugis.addis.base.AbstractAddisCoreController;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.workspaceSettings.WorkspaceSettings;
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
  public WorkspaceSettings get(@PathVariable Integer analysisId) {
    return workspaceSettingsRepository.get(analysisId);
  }

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}/workspaceSettings", method = RequestMethod.PUT)
  @ResponseBody
  public void put(Principal principal, @PathVariable Integer projectId, @PathVariable Integer analysisId,
                  @RequestBody String workspaceSettings) throws ResourceDoesNotExistException, MethodNotAllowedException {
    projectService.checkOwnership(projectId, principal);
    workspaceSettingsRepository.put(analysisId, workspaceSettings);
  }
}

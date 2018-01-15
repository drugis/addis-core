package org.drugis.addis.toggledColumns.controller;

import org.drugis.addis.base.AbstractAddisCoreController;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.toggledColumns.ToggledColumns;
import org.drugis.addis.toggledColumns.repository.ToggledColumnsRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.security.Principal;

@Controller
public class ToggledColumnsController extends AbstractAddisCoreController {
  @Inject
  private ToggledColumnsRepository toggledColumnsRepository;

  @Inject
  private ProjectService projectService;

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}/toggledColumns", method = RequestMethod.GET)
  @ResponseBody
  public ToggledColumns get(@PathVariable Integer analysisId) {
    return toggledColumnsRepository.get(analysisId);
  }

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}/toggledColumns", method = RequestMethod.PUT)
  @ResponseBody
  public void put(Principal principal,@PathVariable Integer projectId ,@PathVariable Integer analysisId,
                  @RequestBody String toggledColumns) throws ResourceDoesNotExistException, MethodNotAllowedException {
    projectService.checkOwnership(projectId, principal);
    toggledColumnsRepository.put(analysisId, toggledColumns);
  }


}

package org.drugis.addis.scaledUnits.controller;

import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.scaledUnits.ScaledUnit;
import org.drugis.addis.scaledUnits.ScaledUnitCommand;
import org.drugis.addis.scaledUnits.repository.ScaledUnitRepository;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.List;

/**
 * Created by joris on 19-4-17.
 */
@Controller
@Transactional("ptmAddisCore")
public class ScaledUnitController {

  @Inject
  private ScaledUnitRepository scaledUnitRepository;

  @Inject
  private ProjectService projectService;

  @RequestMapping(value = "/projects/{projectId}/scaledUnits", method = RequestMethod.GET)
  @ResponseBody
  public List<ScaledUnit> query(@PathVariable Integer projectId) throws MethodNotAllowedException, ResourceDoesNotExistException {
    return scaledUnitRepository.query(projectId);
  }

  @RequestMapping(value = "/projects/{projectId}/scaledUnits", method = RequestMethod.POST)
  public void create(@PathVariable Integer projectId, @RequestBody ScaledUnitCommand unitCommand,
                     Principal user, HttpServletResponse response) throws ResourceDoesNotExistException, MethodNotAllowedException {
    projectService.checkOwnership(projectId, user);
    scaledUnitRepository.create(projectId, unitCommand.getConceptUri(), unitCommand.multiplier, unitCommand.getName());
    response.setStatus(HttpServletResponse.SC_CREATED);
  }
}

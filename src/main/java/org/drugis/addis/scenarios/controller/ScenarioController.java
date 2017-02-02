package org.drugis.addis.scenarios.controller;

import org.drugis.addis.base.AbstractAddisCoreController;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.scenarios.Scenario;
import org.drugis.addis.scenarios.repository.ScenarioRepository;
import org.drugis.addis.scenarios.service.ScenarioService;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.Collection;

/**
 * Created by connor on 3-4-14.
 */
@Controller
@Transactional("ptmAddisCore")
public class ScenarioController extends AbstractAddisCoreController {

  @Inject
  ScenarioRepository scenarioRepository;

  @Inject
  ScenarioService scenarioService;

  @Inject
  ProjectService projectService;

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}/scenarios/{scenarioId}", method = RequestMethod.GET)
  @ResponseBody
  public Scenario get(@PathVariable Integer scenarioId) {
    return scenarioRepository.get(scenarioId);
  }

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}/scenarios", method = RequestMethod.GET)
  @ResponseBody
  public Collection<Scenario> query(@PathVariable Integer projectId, @PathVariable Integer analysisId) {
    return scenarioRepository.queryByProjectAndAnalysis(projectId, analysisId);
  }

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}/scenarios/{scenarioId}", method = RequestMethod.POST)
  @ResponseBody
  public Scenario update(Principal principal, @PathVariable Integer projectId, @PathVariable Integer analysisId, @RequestBody Scenario scenario)
    throws ResourceDoesNotExistException, MethodNotAllowedException {
    scenarioService.checkCoordinates(projectId, analysisId, scenario);
    projectService.checkOwnership(projectId, principal);
    return scenarioRepository.update(scenario.getId(), scenario.getTitle(), scenario.getState());
  }

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}/scenarios", method = RequestMethod.POST)
  @ResponseBody
  public Scenario create(Principal principal, HttpServletResponse response, @PathVariable Integer projectId, @PathVariable Integer analysisId, @RequestBody Scenario scenario)
    throws ResourceDoesNotExistException, MethodNotAllowedException {
    scenario.setWorkspace(analysisId);
    scenarioService.checkCoordinates(projectId, analysisId, scenario);
    projectService.checkOwnership(projectId, principal);
    Scenario result = scenarioRepository.create(analysisId, scenario.getTitle(), scenario.getState());
    response.setStatus(HttpServletResponse.SC_CREATED);
    return result;
  }

}

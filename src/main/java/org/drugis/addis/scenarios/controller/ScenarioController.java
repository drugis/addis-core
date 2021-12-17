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
  private ScenarioRepository scenarioRepository;

  @Inject
  private ScenarioService scenarioService;

  @Inject
  private ProjectService projectService;

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}/problems/{subProblemId}/scenarios/{scenarioId}", method = RequestMethod.GET)
  @ResponseBody
  public Scenario get(@PathVariable Integer scenarioId) {
    return scenarioRepository.get(scenarioId);
  }

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}/problems/{subProblemId}/scenarios", method = RequestMethod.GET)
  @ResponseBody
  public Collection<Scenario> queryBySubProblem(@PathVariable(value="projectId") Integer projectId,
                                                @PathVariable(value="analysisId") Integer analysisId,
                                                @PathVariable Integer subProblemId) {
    return scenarioRepository.queryBySubProblem(projectId, analysisId, subProblemId);
  }

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}/scenarios", method = RequestMethod.GET)
  @ResponseBody
  public Collection<Scenario> queryByAnalysis(@PathVariable(value="projectId") Integer projectId,
                                              @PathVariable(value="analysisId") Integer analysisId) {
    return scenarioRepository.queryByAnalysis(projectId, analysisId);
  }

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}/problems/{subProblemId}/scenarios/{scenarioId}", method = RequestMethod.POST)
  @ResponseBody
  public Scenario update(Principal principal, @PathVariable(value="projectId") Integer projectId,
                         @PathVariable(value="analysisId") Integer analysisId,
                         @PathVariable Integer subProblemId,
                         @RequestBody Scenario scenario)
          throws ResourceDoesNotExistException, MethodNotAllowedException {
    scenarioService.checkCoordinates(projectId, analysisId, subProblemId, scenario);
    projectService.checkOwnership(projectId, principal);
    return scenarioRepository.update(scenario.getId(), scenario.getTitle(), scenario.getState());
  }

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}/problems/{subProblemId}/scenarios", method = RequestMethod.POST)
  @ResponseBody
  public Scenario create(Principal principal, HttpServletResponse response, @PathVariable(value="projectId") Integer projectId,
                         @PathVariable(value="analysisId") Integer analysisId,
                         @PathVariable Integer subProblemId, @RequestBody Scenario scenario)
          throws ResourceDoesNotExistException, MethodNotAllowedException {
    scenario.setWorkspace(analysisId);
    projectService.checkOwnership(projectId, principal);
    scenarioService.checkCoordinates(projectId, analysisId, subProblemId, scenario);
    Scenario result = scenarioRepository.create(analysisId, subProblemId, scenario.getTitle(), scenario.getState());
    response.setStatus(HttpServletResponse.SC_CREATED);
    return result;
  }

}

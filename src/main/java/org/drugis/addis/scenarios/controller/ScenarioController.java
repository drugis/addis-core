package org.drugis.addis.scenarios.controller;

import org.drugis.addis.base.AbstractAddisCoreController;
import org.drugis.addis.scenarios.Scenario;
import org.drugis.addis.scenarios.repository.ScenarioRepository;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Collection;

/**
 * Created by connor on 3-4-14.
 */
@Controller
@Transactional("ptmAddisCore")
public class ScenarioController extends AbstractAddisCoreController {

  @Inject
  ScenarioRepository scenarioRepository;

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}/scenarios/{scenarioId}", method = RequestMethod.GET)
  @ResponseBody
  public Scenario get(@PathVariable Integer scenarioId) {
    return scenarioRepository.get(scenarioId);
  }

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}/scenarios", method = RequestMethod.GET)
  @ResponseBody
  public Collection<Scenario> query(@PathVariable Integer projectId, @PathVariable Integer analysisId) {
    return scenarioRepository.query(projectId, analysisId);
  }
}

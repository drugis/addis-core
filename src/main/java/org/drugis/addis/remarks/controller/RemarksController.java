package org.drugis.addis.remarks.controller;

import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.base.AbstractAddisCoreController;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.remarks.Remarks;
import org.drugis.addis.remarks.repository.RemarksRepository;
import org.drugis.addis.scenarios.Scenario;
import org.drugis.addis.scenarios.repository.ScenarioRepository;
import org.drugis.addis.scenarios.service.ScenarioService;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;

/**
 * Created by daan on 16-9-14.
 */
@Controller
@Transactional("ptmAddisCore")
public class RemarksController extends AbstractAddisCoreController {

  @Inject
  RemarksRepository remarksRepository;

  @Inject
  AnalysisService analysisService;

  @Inject
  ProjectService projectService;

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}/remarks", method = RequestMethod.GET)
  @ResponseBody
  public Remarks getRemarks(@PathVariable Integer analysisId) {
    return remarksRepository.find(analysisId);
  }

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}/remarks", method = RequestMethod.POST)
  @ResponseBody
  public Remarks saveRemarks(Principal principal, HttpServletResponse response, @PathVariable Integer projectId,
                             @PathVariable Integer analysisId, @RequestBody Remarks remarks) throws ResourceDoesNotExistException, MethodNotAllowedException {
    analysisService.checkCoordinates(projectId, analysisId);
    projectService.checkOwnership(projectId, principal);

    if (remarksRepository.find(remarks.getAnalysisId()) != null) {
      return remarksRepository.update(remarks);
    } else {
      Remarks created = remarksRepository.create(analysisId, remarks.getRemarks());
      response.setStatus(HttpServletResponse.SC_CREATED);
      return created;
    }
  }
}

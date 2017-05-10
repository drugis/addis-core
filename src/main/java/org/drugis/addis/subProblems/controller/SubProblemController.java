package org.drugis.addis.subProblems.controller;

import org.apache.http.HttpStatus;
import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.base.AbstractAddisCoreController;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.subProblems.SubProblem;
import org.drugis.addis.subProblems.controller.command.SubProblemCommand;
import org.drugis.addis.subProblems.repository.SubProblemRepository;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.Collection;

/**
 * Created by joris on 8-5-17.
 */
@Controller
@Transactional("ptmAddisCore")
public class SubProblemController extends AbstractAddisCoreController {

  @Inject
  private SubProblemRepository subProblemRepository;

  @Inject
  private ProjectService projectService;

  @Inject
  private AnalysisService analysisService;

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}/problems/{subProblemId}", method = RequestMethod.GET)
  @ResponseBody
  public SubProblem get(@PathVariable Integer subProblemId) throws ResourceDoesNotExistException {
    return subProblemRepository.get(subProblemId);
  }

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}/problems", method = RequestMethod.GET)
  @ResponseBody
  public Collection<SubProblem> query(@PathVariable Integer projectId, @PathVariable Integer analysisId) {
    return subProblemRepository.queryByProjectAndAnalysis(projectId, analysisId);
  }

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}/problems", method = RequestMethod.POST)
  @ResponseBody
  public SubProblem create(Principal principal, HttpServletResponse response,
                           @PathVariable Integer projectId, @PathVariable Integer analysisId,
                           @RequestBody SubProblemCommand subProblemCommand) throws ResourceDoesNotExistException, MethodNotAllowedException {
    projectService.checkOwnership(projectId, principal);
    analysisService.checkCoordinates(projectId, analysisId);
    response.setStatus(HttpStatus.SC_CREATED);
    return subProblemRepository.create(analysisId, subProblemCommand.getDefinition(), subProblemCommand.getTitle());
  }

}

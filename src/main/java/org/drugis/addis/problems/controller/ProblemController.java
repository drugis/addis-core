package org.drugis.addis.problems.controller;

import org.drugis.addis.base.AbstractAddisCoreController;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.problems.model.Problem;
import org.drugis.addis.problems.service.ProblemService;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

/**
 * Created by daan on 3/21/14.
 */
@Controller
@Transactional("ptmAddisCore")
public class ProblemController extends AbstractAddisCoreController {

  @Inject
  private ProblemService problemService;

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}/problem", method = RequestMethod.GET)
  @ResponseBody
  public Problem get(@PathVariable Integer projectId, @PathVariable Integer analysisId) throws ResourceDoesNotExistException {
    return problemService.getProblem(projectId, analysisId);
  }
}

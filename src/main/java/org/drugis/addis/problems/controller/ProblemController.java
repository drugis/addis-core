package org.drugis.addis.problems.controller;

import org.drugis.addis.base.AbstractAddisCoreController;
import org.drugis.addis.exception.ProblemCreationException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.problems.model.AbstractProblem;
import org.drugis.addis.problems.service.HostURLCache;
import org.drugis.addis.problems.service.ProblemService;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;

/**
 * Created by daan on 3/21/14.
 */
@Controller
@Transactional("ptmAddisCore")
public class ProblemController extends AbstractAddisCoreController {

  @Inject
  private ProblemService problemService;

  @Inject
  private HostURLCache hostURLCache;

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}/problem", method = RequestMethod.GET)
  @ResponseBody
  public AbstractProblem get(@PathVariable Integer projectId, @PathVariable Integer analysisId, HttpServletRequest request) throws ResourceDoesNotExistException, ProblemCreationException, MalformedURLException {
    hostURLCache.setHostFromRequestUrl(request.getRequestURL().toString());
    return problemService.getProblem(projectId, analysisId);
  }
}

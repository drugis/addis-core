package org.drugis.addis.problems.controller;

import org.drugis.addis.base.AbstractAddisCoreController;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.service.impl.InvalidTypeForDoseCheckException;
import org.drugis.addis.problems.model.AbstractProblem;
import org.drugis.addis.problems.service.ProblemService;
import org.drugis.addis.trialverse.service.impl.ReadValueException;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

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
  public AbstractProblem get(@PathVariable Integer projectId, @PathVariable Integer analysisId) throws ResourceDoesNotExistException, URISyntaxException, SQLException, IOException, ReadValueException, InvalidTypeForDoseCheckException {
    return problemService.getProblem(projectId, analysisId);
  }
}

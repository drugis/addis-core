package org.drugis.addis.models.controller;

import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.base.AbstractAddisCoreController;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.models.Model;
import org.drugis.addis.models.service.ModelService;
import org.drugis.addis.projects.service.ProjectService;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;

/**
 * Created by daan on 22-5-14.
 */
@Controller
@Transactional("ptmAddisCore")
public class ModelController extends AbstractAddisCoreController {

  @Inject
  AnalysisService analysisService;

  @Inject
  ProjectService projectService;

  @Inject
  ModelService modelService;

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}/models", method = RequestMethod.POST)
  @ResponseBody
  public Model create(HttpServletResponse response, Principal principal, @PathVariable Integer projectId, @PathVariable Integer analysisId) throws ResourceDoesNotExistException, MethodNotAllowedException {
    projectService.checkOwnership(projectId, principal);
    analysisService.checkCoordinates(projectId, analysisId);
    Model createdModel = modelService.createModel(projectId, analysisId);
    response.setStatus(HttpServletResponse.SC_CREATED);
    return createdModel;
  }

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}/models/{modelId}", method = RequestMethod.GET)
  @ResponseBody
  public Model get(@PathVariable Integer analysisId, @PathVariable Integer modelId) throws MethodNotAllowedException, ResourceDoesNotExistException {
    Model model = modelService.getModel(analysisId, modelId);
    return model;
  }
}

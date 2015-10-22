package org.drugis.addis.models.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.http.HttpHeaders;
import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.base.AbstractAddisCoreController;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.models.Model;
import org.drugis.addis.models.ModelCommand;
import org.drugis.addis.models.exceptions.InvalidHeterogeneityTypeException;
import org.drugis.addis.models.exceptions.InvalidModelTypeException;
import org.drugis.addis.models.service.ModelService;
import org.drugis.addis.projects.service.ProjectService;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.sql.SQLException;
import java.util.List;

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
  public Model create(HttpServletResponse response, Principal principal, @PathVariable Integer projectId, @PathVariable Integer analysisId, @RequestBody ModelCommand modelCommand) throws ResourceDoesNotExistException, MethodNotAllowedException, JsonProcessingException, InvalidModelTypeException, InvalidHeterogeneityTypeException {
    projectService.checkOwnership(projectId, principal);
    analysisService.checkCoordinates(projectId, analysisId);
    Model createdModel = modelService.createModel(analysisId, modelCommand);
    response.setStatus(HttpServletResponse.SC_CREATED);
    response.addHeader(HttpHeaders.LOCATION, "/projects/" + projectId + "/analyses/" + analysisId + "/models/" + createdModel.getId());
    return createdModel;
  }

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}/models/{modelId}", method = RequestMethod.GET)
  @ResponseBody
  public Model get(@PathVariable Integer analysisId, @PathVariable Integer modelId) throws MethodNotAllowedException, ResourceDoesNotExistException {
    return modelService.getModel(analysisId, modelId);
  }

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}/models", method = RequestMethod.GET)
  @ResponseBody
  public List<Model> query(@PathVariable Integer analysisId) throws SQLException {
    return modelService.query(analysisId);
  }
}

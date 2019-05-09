package org.drugis.addis.models.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.base.AbstractAddisCoreController;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.models.FunnelPlot;
import org.drugis.addis.models.Model;
import org.drugis.addis.models.ModelTitleCommand;
import org.drugis.addis.models.controller.command.CreateFunnelPlotCommand;
import org.drugis.addis.models.controller.command.CreateModelCommand;
import org.drugis.addis.models.controller.command.UpdateModelCommand;
import org.drugis.addis.models.exceptions.InvalidModelException;
import org.drugis.addis.models.repository.FunnelPlotRepository;
import org.drugis.addis.models.repository.ModelRepository;
import org.drugis.addis.models.service.ModelService;
import org.drugis.addis.patavitask.repository.PataviTaskRepository;
import org.drugis.addis.patavitask.repository.UnexpectedNumberOfResultsException;
import org.drugis.addis.models.ModelBaseline;
import org.drugis.addis.models.repository.ModelBaselineRepository;
import org.drugis.addis.projects.service.ProjectService;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.Principal;
import java.sql.SQLException;
import java.util.List;

;

/**
 * Created by daan on 22-5-14.
 */
@Controller
@Transactional("ptmAddisCore")
public class ModelController extends AbstractAddisCoreController {

  @Inject
  private AnalysisService analysisService;

  @Inject
  private ModelService modelService;

  @Inject
  private ProjectService projectService;

  @Inject
  private PataviTaskRepository pataviTaskRepository;

  @Inject
  private ModelRepository modelRepository;

  @Inject
  private FunnelPlotRepository funnelPlotRepository;

  @Inject
  private ModelBaselineRepository modelBaselineRepository;

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}/models", method = RequestMethod.POST)
  @ResponseBody
  public Model create(HttpServletResponse response, Principal principal, @PathVariable Integer projectId,
                      @PathVariable Integer analysisId, @RequestBody CreateModelCommand createModelCommand) throws ResourceDoesNotExistException, MethodNotAllowedException, InvalidModelException {
    projectService.checkOwnership(projectId, principal);
    analysisService.checkCoordinates(projectId, analysisId);
    Model createdModel = modelService.createModel(analysisId, createModelCommand);
    response.setStatus(HttpServletResponse.SC_CREATED);
    response.addHeader(HttpHeaders.LOCATION, "/projects/" + projectId + "/analyses/" + analysisId + "/models/" + createdModel.getId());
    return createdModel;
  }

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}/models/{modelId}", method = RequestMethod.GET)
  @ResponseBody
  public Model get(@PathVariable Integer modelId) throws MethodNotAllowedException, ResourceDoesNotExistException, IOException, SQLException {
    return modelService.get(modelId);
  }

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}/models", method = RequestMethod.GET)
  @ResponseBody
  public List<Model> query(@PathVariable Integer analysisId) throws SQLException, IOException {
    return modelService.query(analysisId);
  }

  @RequestMapping(value = "/projects/{projectId}/consistencyModels", method = RequestMethod.GET)
  @ResponseBody
  public List<Model> consistencyModels(@PathVariable Integer projectId) throws SQLException, IOException {
    return modelService.queryConsistencyModels(projectId);
  }

  @RequestMapping(value = "/projects/{projectId}/models", method = RequestMethod.GET)
  @ResponseBody
  public List<Model> queryModelsByProject(@PathVariable Integer projectId) throws SQLException, IOException {
    return modelService.queryModelsByProject(projectId);
  }


  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}/models/{modelId}", method = RequestMethod.POST)
  @ResponseBody
  public void update(Principal principal, @RequestBody UpdateModelCommand updateModelCommand) throws MethodNotAllowedException, ResourceDoesNotExistException, InvalidModelException, IOException {
    modelService.checkOwnership(updateModelCommand.getId(), principal);
    modelService.increaseRunLength(updateModelCommand);
  }

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}/models/{modelId}/attributes", method = RequestMethod.POST)
  @ResponseBody
  public void setAttributes(Principal principal, @PathVariable Integer modelId, @RequestBody ModelAttributesCommand modelAttributesCommand) throws MethodNotAllowedException, ResourceDoesNotExistException, IOException {
    modelService.checkOwnership(modelId, principal);
    modelRepository.setArchived(modelId, modelAttributesCommand.getArchived());
  }

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}/models/{modelId}/funnelPlots", method = RequestMethod.POST)
  public void createFunnelPlot(HttpServletResponse response, Principal principal,
                               @PathVariable Integer projectId, @PathVariable Integer modelId, @PathVariable Integer analysisId, @RequestBody CreateFunnelPlotCommand createFunnelPlotCommand) throws ResourceDoesNotExistException, MethodNotAllowedException, IOException {
    modelService.checkOwnership(modelId, principal);
    analysisService.checkCoordinates(projectId, analysisId);
    funnelPlotRepository.create(createFunnelPlotCommand);
    response.setStatus(HttpStatus.SC_CREATED);
  }

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}/models/{modelId}/funnelPlots", method = RequestMethod.GET)
  @ResponseBody
  public List<FunnelPlot> queryFunnelPlots(@PathVariable Integer modelId) {
    return funnelPlotRepository.query(modelId);
  }

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}/models/{modelId}/result", method = RequestMethod.GET)
  @ResponseBody
  public JsonNode getResult(HttpServletResponse response, @PathVariable Integer modelId) throws ResourceDoesNotExistException, IOException, URISyntaxException, SQLException {
    Model model = modelService.get(modelId);
    if (model.getTaskUrl() != null) {
      try {
        return pataviTaskRepository.getResult(model.getTaskUrl());
      } catch (UnexpectedNumberOfResultsException e) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return null;
      }
    } else {
      throw new ResourceDoesNotExistException("attempt to get results of model with no task");
    }
  }

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}/models/{modelId}/baseline", method = RequestMethod.PUT)
  public void setBaseline(HttpServletResponse response, Principal principal, @PathVariable Integer modelId, @RequestBody String baseline) throws IOException, ResourceDoesNotExistException, MethodNotAllowedException {
    modelService.checkOwnership(modelId, principal);
    modelBaselineRepository.setModelBaseline(modelId, baseline);
    response.setStatus(HttpStatus.SC_OK);
  }

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}/models/{modelId}/baseline", method = RequestMethod.GET)
  @ResponseBody
  public ModelBaseline getBaseline(@PathVariable Integer modelId) {
    return modelBaselineRepository.getModelBaseline(modelId);
  }

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}/models/{modelId}/setTitle", method = RequestMethod.PUT)
  public void setTitle(HttpServletResponse response, Principal principal, @PathVariable Integer modelId, @RequestBody ModelTitleCommand titleCommand) throws MethodNotAllowedException, IOException, ResourceDoesNotExistException {
    modelService.checkOwnership(modelId, principal);
    modelRepository.setTitle(modelId, titleCommand.getNewTitle());
    response.setStatus(HttpStatus.SC_OK);
  }
}

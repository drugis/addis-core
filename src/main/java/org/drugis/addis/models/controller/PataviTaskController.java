package org.drugis.addis.models.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.models.PataviTaskUriHolder;
import org.drugis.addis.models.service.PataviTaskService;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

/**
 * Created by connor on 26-6-14.
 */
@Controller
@Transactional("ptmAddisCore")
public class PataviTaskController {

  @Inject
  PataviTaskService pataviTaskService;

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}/models/{modelId}/task", method = RequestMethod.GET)
  @ResponseBody
  public PataviTaskUriHolder get(@PathVariable Integer projectId, @PathVariable Integer analysisId, @PathVariable Integer modelId) throws ResourceDoesNotExistException, JsonProcessingException {
    return pataviTaskService.getPataviTaskUriHolder(projectId, analysisId, modelId);
  }

}

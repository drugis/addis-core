package org.drugis.addis.patavitask.controller;

import org.drugis.addis.interventions.service.impl.InvalidTypeForDoseCheckException;
import org.drugis.addis.patavitask.PataviTaskUriHolder;
import org.drugis.addis.patavitask.service.PataviTaskService;
import org.drugis.addis.trialverse.service.impl.ReadValueException;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;

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
  public PataviTaskUriHolder get(@PathVariable Integer projectId, @PathVariable Integer analysisId, @PathVariable Integer modelId) throws Exception, ReadValueException, InvalidTypeForDoseCheckException {
    return pataviTaskService.getGemtcPataviTaskUriHolder(projectId, analysisId, modelId);
  }

  @RequestMapping(value="/patavi", method = RequestMethod.POST)
  @ResponseBody
  public void mcdaTask(HttpServletResponse response, @RequestBody Object problem) {
    JSONObject jsonProblem = new JSONObject((LinkedHashMap)problem); // can't directly use JSONObject as requestbody because it doesn't get deserialised
    PataviTaskUriHolder mcdaPataviTaskUriHolder = pataviTaskService.getMcdaPataviTaskUriHolder(jsonProblem);
    response.setHeader("location", mcdaPataviTaskUriHolder.getUri().toString());
    response.setStatus(HttpServletResponse.SC_CREATED);
  }

}

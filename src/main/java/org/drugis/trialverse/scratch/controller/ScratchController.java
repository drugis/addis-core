package org.drugis.trialverse.scratch.controller;

import org.drugis.trialverse.scratch.service.ScratchService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Controller
@RequestMapping(value = "/scratch")
public class ScratchController {

  @Inject
  ScratchService scratchService;

  @RequestMapping(method = RequestMethod.POST)
  @ResponseBody
  public void proxy(HttpServletRequest request, HttpServletResponse response) {
    scratchService.proxyPost(request, response);
  }
}



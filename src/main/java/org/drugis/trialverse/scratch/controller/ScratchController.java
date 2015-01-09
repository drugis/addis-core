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

  @RequestMapping(value="/update", method = RequestMethod.POST)
  @ResponseBody
  public void proxyUpdate(HttpServletRequest request, HttpServletResponse response) {
    scratchService.proxyUpdate(request, response);
  }

  @RequestMapping(value="/data", method = RequestMethod.POST)
  @ResponseBody
  public void proxyData(HttpServletRequest request, HttpServletResponse response) {
    scratchService.proxyData(request, response);
  }

  @RequestMapping(value="/query", method = RequestMethod.POST)
  @ResponseBody
  public void proxyQuery(HttpServletRequest request, HttpServletResponse response) {
    scratchService.proxyQuery(request, response);
  }

  @RequestMapping(method = RequestMethod.GET)
  @ResponseBody
  public void proxyGetGraph(HttpServletRequest request, HttpServletResponse response) {
    scratchService.proxyGetGraph(request, response);
  }

}



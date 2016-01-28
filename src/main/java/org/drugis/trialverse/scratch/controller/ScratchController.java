package org.drugis.trialverse.scratch.controller;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.drugis.trialverse.scratch.service.ScratchService;
import org.drugis.trialverse.util.WebConstants;
import org.drugis.trialverse.util.service.TrialverseIOUtilsService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;


@Controller
@RequestMapping(value = "/scratch")
public class ScratchController {

  @Inject
  ScratchService scratchService;

  @Inject
  TrialverseIOUtilsService trialverseIOUtilsService;

  @RequestMapping(value = "/update", method = RequestMethod.POST)
  @ResponseBody
  public void proxyUpdate(@RequestHeader(value = "Content-type") String contentTypeStr,
                          HttpServletRequest request, HttpServletResponse response) throws IOException {
    Header contentTypeHeader = new BasicHeader("Content-Type", contentTypeStr);
    try (InputStream inputstream = request.getInputStream()) {
      byte[] content = IOUtils.toByteArray(inputstream);
      int statusCode = scratchService.update(content, request.getQueryString(), contentTypeHeader);
      response.setStatus(statusCode);
    }
  }

  @RequestMapping(value = "/data", method = RequestMethod.POST)
  @ResponseBody
  public void proxyData(HttpServletRequest request, HttpServletResponse response,
                        @RequestHeader(value = "Content-type") String contentTypeStr) throws IOException {
    Header contentTypeHeader = new BasicHeader("Content-Type", contentTypeStr);
    try (InputStream inputstream = request.getInputStream()) {
      byte[] requestContent = IOUtils.toByteArray(inputstream);
      int statusCode = scratchService.setData(requestContent, request.getQueryString(), contentTypeHeader);
      response.setStatus(statusCode);
    }
  }

  @RequestMapping(value = "/query", method = RequestMethod.POST)
  @ResponseBody
  public void proxyQuery(HttpServletRequest request, HttpServletResponse response,
                         @RequestHeader(value = "Content-type") String contentTypeStr,
                         @RequestHeader(value = "Accept") String acceptStr) throws IOException {
    Header acceptHeader = new BasicHeader("Accept", acceptStr);
    Header contentTypeHeader = new BasicHeader("Content-Type", contentTypeStr);
    try (InputStream inputstream = request.getInputStream()) {
      byte[] requestContent = IOUtils.toByteArray(inputstream);
      byte[] responseContent = scratchService.query(requestContent, request.getQueryString(), contentTypeHeader, acceptHeader);
      trialverseIOUtilsService.writeContentToServletResponse(responseContent, response);
    }
  }

  @RequestMapping(method = RequestMethod.GET)
  @ResponseBody
  public void proxyGetGraph(HttpServletRequest request, HttpServletResponse response,
                            @RequestHeader(value = "Accept") String acceptStr) throws IOException {
    Header acceptHeader = new BasicHeader("Accept", acceptStr);
    byte[] responseContent = scratchService.get(request.getQueryString(), acceptHeader);
    trialverseIOUtilsService.writeContentToServletResponse(responseContent, response);
  }

}



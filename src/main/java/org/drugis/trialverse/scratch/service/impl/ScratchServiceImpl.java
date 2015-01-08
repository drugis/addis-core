package org.drugis.trialverse.scratch.service.impl;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.jena.riot.RDFLanguages;
import org.drugis.trialverse.dataset.factory.HttpClientFactory;
import org.drugis.trialverse.scratch.service.ScratchService;
import org.drugis.trialverse.util.service.TrialverseIOUtilsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by connor on 08/01/15.
 */
@Service
public class ScratchServiceImpl implements ScratchService {


  final static Logger logger = LoggerFactory.getLogger(ScratchServiceImpl.class);

  @Inject
  private HttpClientFactory httpClientFactory;

  @Inject
  private TrialverseIOUtilsService trialverseIOUtilsService;

  @Override
  public void proxyPost(HttpServletRequest httpServletRequest, HttpServletResponse response) {

    try (ServletInputStream servletInputStream = httpServletRequest.getInputStream()) {
      HttpPost request = new HttpPost("http://box006.drugis.org:3031/ds/data?" + httpServletRequest.getQueryString());
      request.setHeader("Accept", httpServletRequest.getHeader("Accept"));
      HttpClient httpClient = httpClientFactory.build();
      InputStreamEntity entity = new InputStreamEntity(servletInputStream);
      entity.setContentType(httpServletRequest.getContentType());
      request.setEntity(entity);
      HttpResponse httpResponse = httpClient.execute(request);
      response.setStatus(httpResponse.getStatusLine().getStatusCode());
      trialverseIOUtilsService.writeResponseContentToServletResponse(httpResponse, response);
    } catch (IOException e) {
      logger.error(e.toString());
    }
  }
}

package org.drugis.trialverse.scratch.service.impl;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.drugis.trialverse.dataset.factory.HttpClientFactory;
import org.drugis.trialverse.scratch.service.ScratchService;
import org.drugis.trialverse.util.service.TrialverseIOUtilsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
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
  public static final String FUSEKI_SCRATCH_URL = System.getenv("TRIALVERSE_SCRATCH_URL");
  public static final String FUSEKI_SCRATCH_UPDATE_URL = FUSEKI_SCRATCH_URL + "/ds/update?";
  public static final String FUSEKI_SCRATCH_DATA_URL = FUSEKI_SCRATCH_URL + "/ds/data?";
  public static final String FUSEKI_SCRATCH_QUERY_URL = FUSEKI_SCRATCH_URL + "/ds/query?";

  @Inject
  private HttpClientFactory httpClientFactory;

  @Inject
  private TrialverseIOUtilsService trialverseIOUtilsService;

  private void executePost(HttpServletRequest httpServletRequest, HttpServletResponse response, String url) {
    try (ServletInputStream servletInputStream = httpServletRequest.getInputStream()) {
      HttpPost request = new HttpPost(url + httpServletRequest.getQueryString());
      request.setHeader("Accept", httpServletRequest.getHeader("Accept"));
      HttpClient httpClient = httpClientFactory.build();
      InputStreamEntity entity = new InputStreamEntity(servletInputStream);
      entity.setContentType(httpServletRequest.getContentType());
      request.setEntity(entity);
      HttpResponse httpResponse = httpClient.execute(request);
      response.setStatus(httpResponse.getStatusLine().getStatusCode());
      if (httpResponse.getEntity() != null) {
        trialverseIOUtilsService.writeResponseContentToServletResponse(httpResponse, response);
      }
    } catch (IOException e) {
      logger.error(e.toString());
    }
  }

  private void executeGet(HttpServletRequest httpServletRequest, HttpServletResponse response, String url) {
    try {
      HttpGet request = new HttpGet(url + httpServletRequest.getQueryString());
      request.setHeader("Accept", httpServletRequest.getHeader("Accept"));
      HttpClient httpClient = httpClientFactory.build();
      HttpResponse httpResponse = httpClient.execute(request);
      response.setStatus(httpResponse.getStatusLine().getStatusCode());
      trialverseIOUtilsService.writeResponseContentToServletResponse(httpResponse, response);
    } catch (IOException e) {
      logger.error(e.toString());
    }
  }

  @Override
  public void proxyUpdate(HttpServletRequest httpServletRequest, HttpServletResponse response) {
    executePost(httpServletRequest, response, FUSEKI_SCRATCH_UPDATE_URL);
  }

  @Override
  public void proxyData(HttpServletRequest httpServletRequest, HttpServletResponse response) {
    executePost(httpServletRequest, response, FUSEKI_SCRATCH_DATA_URL);
  }

  @Override
  public void proxyQuery(HttpServletRequest httpServletRequest, HttpServletResponse response) {
    executePost(httpServletRequest, response, FUSEKI_SCRATCH_QUERY_URL);
  }

  @Override
  public void proxyGetGraph(HttpServletRequest request, HttpServletResponse response) {
    executeGet(request, response, FUSEKI_SCRATCH_DATA_URL);
  }
}

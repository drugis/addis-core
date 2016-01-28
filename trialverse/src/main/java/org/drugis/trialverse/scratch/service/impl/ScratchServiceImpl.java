package org.drugis.trialverse.scratch.service.impl;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.util.EntityUtils;
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
import java.io.InputStream;

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
  private HttpClient httpClient;

  @Inject
  private TrialverseIOUtilsService trialverseIOUtilsService;

  @Override
  public int update(byte[] content, String queryString, Header contentTypeHeader) throws IOException {
    HttpPost request = new HttpPost(FUSEKI_SCRATCH_UPDATE_URL + queryString);
    ByteArrayEntity entity = new ByteArrayEntity(content);
    entity.setContentType(contentTypeHeader);
    request.setEntity(entity);
    try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) httpClient.execute(request)) {
      EntityUtils.consume(httpResponse.getEntity());
      return httpResponse.getStatusLine().getStatusCode();
    }
  }

  @Override
  public int setData(byte[] content, String queryString, Header contentTypeHeader) throws IOException {
    ByteArrayEntity entity = new ByteArrayEntity(content);
    entity.setContentType(contentTypeHeader);
    HttpPost request = new HttpPost(FUSEKI_SCRATCH_DATA_URL + queryString);
    request.setEntity(entity);
    try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) httpClient.execute(request)) {
      EntityUtils.consume(httpResponse.getEntity());
      return httpResponse.getStatusLine().getStatusCode();
    }
  }

  @Override
  public byte [] query(byte[] requestContent, String queryString, Header acceptHeader, Header contentTypeHeader) throws IOException {
    HttpPost request = new HttpPost(FUSEKI_SCRATCH_QUERY_URL + queryString);
    request.setHeader(acceptHeader);
    ByteArrayEntity entity = new ByteArrayEntity(requestContent);
    entity.setContentType(contentTypeHeader);
    request.setEntity(entity);
    try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) httpClient.execute(request);
         InputStream responseContentStream = httpResponse.getEntity().getContent()) {
      byte[] responseContent = IOUtils.toByteArray(responseContentStream);
      EntityUtils.consume(httpResponse.getEntity());
      return responseContent;
    }
  }

  @Override
  public byte[] get(String queryString, Header acceptHeader) throws IOException {
    HttpGet request = new HttpGet(FUSEKI_SCRATCH_DATA_URL + queryString);
    request.setHeader(acceptHeader);
    try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) httpClient.execute(request);
         InputStream responseContentStream = httpResponse.getEntity().getContent()) {
      byte[] responseContent = IOUtils.toByteArray(responseContentStream);
      EntityUtils.consume(httpResponse.getEntity());
      return responseContent;
    }
  }

}

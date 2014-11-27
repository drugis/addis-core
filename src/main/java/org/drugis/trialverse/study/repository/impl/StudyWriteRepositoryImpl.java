package org.drugis.trialverse.study.repository.impl;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.InputStreamEntity;
import org.drugis.trialverse.dataset.factory.HttpClientFactory;
import org.drugis.trialverse.study.repository.StudyWriteRepository;
import org.drugis.trialverse.util.WebConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.servlet.ServletRequest;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by daan on 20-11-14.
 */
@Repository
public class StudyWriteRepositoryImpl implements StudyWriteRepository {

  @Inject
  private WebConstants webConstants;

  @Inject
  private HttpClientFactory httpClientFactory;

  private final static Logger logger = LoggerFactory.getLogger(StudyWriteRepositoryImpl.class);

  private String createStudyGraphUri(String studyUUID) {
    URIBuilder builder = null;
    try {
      builder = new URIBuilder(webConstants.getTriplestoreDataUri() + "/data");
      builder.addParameter("graph", "http://trials.drugis.org/studies/" + studyUUID);
      return builder.build().toString();
    } catch (URISyntaxException e) {
      logger.error(e.toString());
    }
    return "";
  }

  private HttpResponse doRequest(ServletRequest servletRequest, HttpEntityEnclosingRequestBase request) {
    HttpClient client = httpClientFactory.build();
    HttpResponse response = null;
    try {
      InputStreamEntity inputStreamEntity = new InputStreamEntity(servletRequest.getInputStream());
      inputStreamEntity.setContentType("application/ld+json");
      request.setEntity(inputStreamEntity);
      request.setHeader("Accept", "application/ld+json");
      response = client.execute(request);
    } catch (IOException e) {
      logger.error(e.toString());
    }
    return response;
  }


  @Override
  public HttpResponse createStudy(String studyUUID, ServletRequest servletRequest) {
    HttpPut request = new HttpPut(createStudyGraphUri(studyUUID));
    HttpResponse response = doRequest(servletRequest, request);
    return response;
  }

  @Override
  public HttpResponse updateStudy(String studyUUID, ServletRequest servletRequest) {
    HttpPost request = new HttpPost(createStudyGraphUri(studyUUID));
    HttpResponse response = doRequest(servletRequest, request);
    return response;
  }
}

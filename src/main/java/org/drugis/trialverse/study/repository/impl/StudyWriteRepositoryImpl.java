package org.drugis.trialverse.study.repository.impl;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.drugis.trialverse.dataset.factory.HttpClientFactory;
import org.drugis.trialverse.study.repository.StudyWriteRepository;
import org.drugis.trialverse.util.WebConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by daan on 20-11-14.
 */
@Repository
public class StudyWriteRepositoryImpl implements StudyWriteRepository {
  @Inject
  private HttpClientFactory httpClientFactory;

  private final static Logger logger = LoggerFactory.getLogger(StudyWriteRepositoryImpl.class);

  private String createGraphUri(String studyUUID) {
    URIBuilder builder = null;
    try {
      builder = new URIBuilder(WebConstants.TRIPLESTORE_BASE_URI + "/data");
      builder.addParameter("graph", "http://trials.drugis.org/studies/" + studyUUID);
      return builder.build().toString();
    } catch (URISyntaxException e) {
      logger.error(e.toString());
    }
    return "";
  }

  private HttpResponse doRequest(String studyContent, HttpEntityEnclosingRequestBase request) {
    try {
      HttpClient client = httpClientFactory.build();
      StringEntity entity = new StringEntity(studyContent, "UTF-8");
      entity.setContentType("application/ld+json");
      request.setEntity(entity);
      request.setHeader("Accept", "application/ld+json");
      return client.execute(request);
    } catch (IOException e) {
      logger.error(e.toString());
    }
    return null;
  }


  @Override
  public void createStudy(String studyUUID, String studyContent) {
    HttpPut request = new HttpPut(createGraphUri(studyUUID));
    doRequest(studyContent, request);
  }

  @Override
  public void updateStudy(String studyUUID, String studyContent) {
    HttpPost request = new HttpPost(createGraphUri(studyUUID));
    doRequest(studyContent, request);
  }
}

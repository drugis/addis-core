package org.drugis.trialverse.study.repository;

import org.apache.http.HttpResponse;

import javax.servlet.ServletRequest;

/**
 * Created by daan on 20-11-14.
 */
public interface StudyWriteRepository {

  public HttpResponse createStudy(String studyUUID, ServletRequest httpServletRequest);

  public HttpResponse updateStudy(String studyUUID, ServletRequest httpServletRequest);
}

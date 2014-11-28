package org.drugis.trialverse.study.repository;

import org.apache.http.HttpResponse;

/**
 * Created by daan on 20-11-14.
 */
public interface StudyWriteRepository {

  public HttpResponse createStudy(String studyUUID, String content);

  public HttpResponse updateStudy(String studyUUID, String content);
}

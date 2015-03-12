package org.drugis.trialverse.study.repository;

import org.apache.http.HttpResponse;

import java.io.InputStream;

/**
 * Created by daan on 20-11-14.
 */
public interface StudyWriteRepository {


  public void updateStudy(String datasetUuid, String studyUUID, InputStream content);
}

package org.drugis.trialverse.study.repository;

/**
 * Created by daan on 20-11-14.
 */
public interface StudyWriteRepository {

  public void createStudy(String studyUUID, String jsonContent);

  public void updateStudy(String studyUUID, String studyContent);
}

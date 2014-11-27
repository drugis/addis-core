package org.drugis.trialverse.study.service;

import org.apache.http.HttpResponse;
import org.springframework.dao.DuplicateKeyException;

import javax.servlet.ServletRequest;
import java.io.IOException;

/**
 * Created by connor on 27-11-14.
 */
public interface StudyService {
  public HttpResponse createStudy(String datasetUUID, String studyUUID, ServletRequest studyContent) throws DuplicateKeyException, IOException;
}

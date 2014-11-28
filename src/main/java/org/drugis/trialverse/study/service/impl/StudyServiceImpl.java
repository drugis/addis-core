package org.drugis.trialverse.study.service.impl;

import com.jayway.jsonpath.JsonPath;
import org.apache.http.HttpResponse;
import org.drugis.trialverse.dataset.repository.DatasetReadRepository;
import org.drugis.trialverse.study.repository.StudyWriteRepository;
import org.drugis.trialverse.study.service.StudyService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;

/**
 * Created by connor on 27-11-14.
 */
@Service
public class StudyServiceImpl implements StudyService {

  @Inject
  private StudyWriteRepository studyWriteRepository;

  @Inject
  private DatasetReadRepository datasetReadRepository;

  @Override
  public HttpResponse createStudy(String datasetUUID, String studyUUID, String content) throws DuplicateKeyException, IOException {
    String shortName = JsonPath.read(content, "$.@graph[0].label");

    if (datasetReadRepository.containsStudyWithShortname(datasetUUID, shortName)) {
      throw new DuplicateKeyException("dataset already contains study with this short name");
    }

    return studyWriteRepository.createStudy(studyUUID, content);
  }
}

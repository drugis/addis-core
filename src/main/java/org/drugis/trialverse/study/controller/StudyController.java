package org.drugis.trialverse.study.controller;

import org.apache.http.HttpResponse;
import org.drugis.trialverse.dataset.repository.DatasetReadRepository;
import org.drugis.trialverse.exception.MethodNotAllowedException;
import org.drugis.trialverse.study.repository.StudyWriteRepository;
import org.drugis.trialverse.study.service.StudyService;
import org.drugis.trialverse.util.controller.AbstractTrialverseController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;

/**
 * Created by daan on 19-11-14.
 */
@Controller
@RequestMapping(value = "/datasets/{datasetUUID}/studies")
public class StudyController extends AbstractTrialverseController {

  @Inject
  private StudyWriteRepository studyWriteRepository;

  @Inject
  private DatasetReadRepository datasetReadRepository;

  @Inject
  private StudyService studyService;


  @RequestMapping(value = "/{studyUUID}", method = RequestMethod.POST)
  public void updateStudy(HttpServletRequest request, HttpServletResponse response, Principal currentUser,
                          @PathVariable String datasetUUID, @PathVariable String studyUUID)
          throws IOException, MethodNotAllowedException {

    String studyContent = readContent(request);

    if (datasetReadRepository.isOwner(datasetUUID, currentUser)) {
      HttpResponse fusekiResponse = studyWriteRepository.updateStudy(studyUUID, studyContent);
      response.setStatus(fusekiResponse.getStatusLine().getStatusCode());
    } else {
      throw new MethodNotAllowedException();
    }

  }

  @RequestMapping(value = "/{studyUUID}", method = RequestMethod.PUT)
  public void createStudy(HttpServletRequest request, HttpServletResponse response, Principal currentUser,
                          @PathVariable String datasetUUID, @PathVariable String studyUUID)
          throws IOException, MethodNotAllowedException {

    String studyContent = readContent(request);

    if (datasetReadRepository.isOwner(datasetUUID, currentUser)) {
      HttpResponse fusekiResponse = studyService.createStudy(datasetUUID, studyUUID, studyContent);
      response.setStatus(fusekiResponse.getStatusLine().getStatusCode());
    } else {
      throw new MethodNotAllowedException();
    }
  }

}

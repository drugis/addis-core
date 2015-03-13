package org.drugis.trialverse.study.controller;

import com.hp.hpl.jena.rdf.model.Model;
import org.apache.http.HttpResponse;
import org.apache.jena.riot.RDFLanguages;
import org.drugis.trialverse.dataset.repository.DatasetReadRepository;
import org.drugis.trialverse.exception.MethodNotAllowedException;
import org.drugis.trialverse.study.repository.StudyReadRepository;
import org.drugis.trialverse.study.repository.StudyWriteRepository;
import org.drugis.trialverse.util.Namespaces;
import org.drugis.trialverse.util.controller.AbstractTrialverseController;
import org.drugis.trialverse.util.service.TrialverseIOUtilsService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;

/**
 * Created by daan on 19-11-14.
 */
@Controller
@RequestMapping(value = "/datasets/{datasetUUID}/studies")
public class StudyController extends AbstractTrialverseController {

  @Inject
  private StudyReadRepository studyReadRepository;

  @Inject
  private StudyWriteRepository studyWriteRepository;

  @Inject
  private DatasetReadRepository datasetReadRepository;

  @Inject
  private TrialverseIOUtilsService trialverseIOUtilsService;


  @RequestMapping(value = "/{studyUUID}", method = RequestMethod.GET)
  @ResponseBody
  public void getStudy(HttpServletResponse response, @PathVariable String studyUUID) {
    // todo maybe check coordinates ?
    Model studyModel = studyReadRepository.getStudy(studyUUID);
    response.setStatus(HttpServletResponse.SC_OK);
    response.setHeader("Content-Type", RDFLanguages.TURTLE.getContentType().getContentType());
    trialverseIOUtilsService.writeModelToServletResponse(studyModel, response);
  }


  @RequestMapping(value = "/{studyUUID}", method = RequestMethod.PUT)
  public void create(HttpServletRequest request, HttpServletResponse response, Principal currentUser,
                     @PathVariable String datasetUUID, @PathVariable String studyUUID)
          throws IOException, MethodNotAllowedException, URISyntaxException {
    URI trialverseDatasetUri = new URI(Namespaces.DATASET_NAMESPACE + datasetUUID);
    if (datasetReadRepository.isOwner(trialverseDatasetUri, currentUser)) {
      studyWriteRepository.updateStudy(new URI(Namespaces.DATASET_NAMESPACE + datasetUUID), studyUUID, request.getInputStream());
      response.setStatus(HttpStatus.OK.value());
    } else {
      throw new MethodNotAllowedException();
    }


  }



}

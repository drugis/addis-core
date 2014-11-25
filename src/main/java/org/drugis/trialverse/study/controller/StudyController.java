package org.drugis.trialverse.study.controller;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.drugis.trialverse.dataset.repository.DatasetReadRepository;
import org.drugis.trialverse.security.Account;
import org.drugis.trialverse.security.repository.AccountRepository;
import org.drugis.trialverse.study.repository.StudyWriteRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.security.Principal;

/**
 * Created by daan on 19-11-14.
 */
@Controller
@RequestMapping(value = "/datasets/{datasetUUID}/studies")
public class StudyController {
  @Inject
  private AccountRepository accountRepository;

  @Inject
  private StudyWriteRepository studyWriteRepository;

  @Inject
  private DatasetReadRepository datasetReadRepository;


  @RequestMapping(value = "/{studyUUID}", method = RequestMethod.POST)
  public void updateStudy(HttpServletRequest request, Principal currentUser,
                          @PathVariable String datasetUUID, @PathVariable String studyUUID) throws IOException {
    Account currentUserAccount = accountRepository.findAccountByUsername(currentUser.getName());
//todo check if user is dataset owner

    BufferedReader reader = request.getReader();
    String studyContent = IOUtils.toString(reader);
    HttpResponse fusekiResponce = studyWriteRepository.updateStudy(studyUUID, studyContent);
  }

  @RequestMapping(value="/{studyUUID}", method = RequestMethod.PUT)
  public void createStudy(HttpServletRequest request, HttpServletResponse response, Principal currentUser, @PathVariable String studyUUID) throws IOException {
    Account currentUserAccount = accountRepository.findAccountByUsername(currentUser.getName());
//todo check if user is dataset owner

    BufferedReader reader = request.getReader();
    String studyContent = IOUtils.toString(reader);
    HttpResponse fusekiResponce = studyWriteRepository.createStudy(studyUUID, studyContent);
    response.setStatus(fusekiResponce.getStatusLine().getStatusCode());
  }
}

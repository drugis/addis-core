package org.drugis.trialverse.dataset.controller;

import org.apache.http.HttpResponse;
import org.drugis.trialverse.dataset.controller.command.DatasetCommand;
import org.drugis.trialverse.dataset.model.Dataset;
import org.drugis.trialverse.dataset.repository.DatasetReadRepository;
import org.drugis.trialverse.dataset.repository.DatasetWriteRepository;
import org.drugis.trialverse.security.Account;
import org.drugis.trialverse.security.repository.AccountRepository;
import org.drugis.trialverse.util.service.TrialverseIOUtilsService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;

/**
 * Created by connor on 6-11-14.
 */
@Controller
@RequestMapping(value = "/datasets")
public class DatasetController {

  @Inject
  private DatasetWriteRepository datasetWriteRepository;

  @Inject
  private DatasetReadRepository datasetReadRepository;

  @Inject
  private TrialverseIOUtilsService trialverseIOUtilsService;

  @Inject
  private AccountRepository accountRepository;

  @RequestMapping(method = RequestMethod.POST)
  @ResponseBody
  public Dataset createDataset(HttpServletRequest request, HttpServletResponse response, Principal currentUser, @RequestBody DatasetCommand datasetCommand) {
    Account currentUserAccount = accountRepository.findAccountByUsername(currentUser.getName());
    String uid = datasetWriteRepository.createDataset(datasetCommand.getTitle(), datasetCommand.getDescription(), currentUserAccount);
    response.setStatus(HttpServletResponse.SC_CREATED);
    response.setHeader("Location", request.getRequestURL() + "/" + uid);
    return new Dataset(uid, currentUserAccount, datasetCommand.getTitle(), datasetCommand.getDescription());
  }

  @RequestMapping(method = RequestMethod.GET)
  @ResponseBody
  public void queryDatasets(HttpServletResponse httpServletResponse, Principal currentUser) {
    Account currentUserAccount = accountRepository.findAccountByUsername(currentUser.getName());
    httpServletResponse.setHeader("Content-Type", "text/turtle");

    HttpResponse response = datasetReadRepository.queryDatasets(currentUserAccount);
    trialverseIOUtilsService.writeResponseContentToServletResponse(response, httpServletResponse);
  }
}

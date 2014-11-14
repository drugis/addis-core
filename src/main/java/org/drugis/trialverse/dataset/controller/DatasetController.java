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
import org.springframework.web.bind.annotation.*;

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
    httpServletResponse.setHeader("Content-Type", "application/ld+json");

    HttpResponse response = datasetReadRepository.queryDatasets(currentUserAccount);
    trialverseIOUtilsService.writeResponseContentToServletResponse(response, httpServletResponse);
  }

  @RequestMapping(value = "/{datasetUUID}", method = RequestMethod.GET)
  @ResponseBody
  public void getDataset(HttpServletResponse httpServletResponse, @PathVariable String datasetUUID) {
    httpServletResponse.setHeader("Content-Type", "application/ld+json");

    HttpResponse response = datasetReadRepository.getDataset(datasetUUID);
    trialverseIOUtilsService.writeResponseContentToServletResponse(response, httpServletResponse);
  }

  @RequestMapping(value = "/{datasetUUID}/studiesWithDetail", method = RequestMethod.GET)
  @ResponseBody
  public void queryStudiesWithDetail(HttpServletResponse httpServletResponse, @PathVariable String datasetUUID) {
    httpServletResponse.setHeader("Content-Type", "application/ld+json");

    HttpResponse response = datasetReadRepository.queryDatasetsWithDetail(datasetUUID);
    trialverseIOUtilsService.writeResponseContentToServletResponse(response, httpServletResponse);
  }


}

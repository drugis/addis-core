package org.drugis.trialverse.dataset.controller;

import com.hp.hpl.jena.rdf.model.Model;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;

import org.apache.http.message.BasicHeader;
import org.apache.jena.riot.RDFLanguages;
import org.drugis.trialverse.dataset.controller.command.DatasetCommand;
import org.drugis.trialverse.dataset.repository.DatasetReadRepository;
import org.drugis.trialverse.dataset.repository.DatasetWriteRepository;
import org.drugis.trialverse.exception.CreateDatasetException;
import org.drugis.trialverse.security.Account;
import org.drugis.trialverse.security.repository.AccountRepository;
import org.drugis.trialverse.util.Namespaces;
import org.drugis.trialverse.util.controller.AbstractTrialverseController;
import org.drugis.trialverse.util.service.TrialverseIOUtilsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;

/**
 * Created by connor on 6-11-14.
 */
@Controller
@RequestMapping(value = "/users/{userUid}/datasets")
public class DatasetController extends AbstractTrialverseController {

  private final static Logger logger = LoggerFactory.getLogger(DatasetController.class);

  @Inject
  private DatasetWriteRepository datasetWriteRepository;

  @Inject
  private DatasetReadRepository datasetReadRepository;

  @Inject
  private TrialverseIOUtilsService trialverseIOUtilsService;

  @Inject
  private AccountRepository accountRepository;

  @Inject
  private HttpClient httpClient;

  @RequestMapping(method = RequestMethod.POST)
  @ResponseBody
  public void createDataset(HttpServletResponse response, Principal currentUser,
                            @RequestBody DatasetCommand datasetCommand, @PathVariable String userUid)
          throws URISyntaxException, CreateDatasetException, HttpException {
    logger.trace("createDataset");
    Account currentUserAccount = accountRepository.findAccountByUsername(currentUser.getName());
    if(currentUserAccount.getuserNameHash().equals(userUid)) {
      URI datasetUri = datasetWriteRepository.createDataset(datasetCommand.getTitle(), datasetCommand.getDescription(), currentUserAccount);
      response.setStatus(HttpServletResponse.SC_CREATED);
      response.setHeader("Location", datasetUri.toString());
    }else {
      logger.error("attempted to created database for user that is not the login-user ");
      response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
  }

  @RequestMapping(method = RequestMethod.GET)
  @ResponseBody
  public void queryDatasetsByUser(HttpServletResponse httpServletResponse, @PathVariable String userUid) {
    logger.trace("retrieving datasets");
    Account user = accountRepository.findAccountByHash(userUid);
    httpServletResponse.setHeader("Content-Type", RDFLanguages.TURTLE.getContentType().getContentType());
    Model model = datasetReadRepository.queryDatasets(user);
    if (model != null) {
      httpServletResponse.setStatus(HttpStatus.OK.value());
    } else {
      httpServletResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
    trialverseIOUtilsService.writeModelToServletResponse(model, httpServletResponse);
    logger.trace("datasets retrieved");
  }

  @RequestMapping(value = "/{datasetUUID}", method = RequestMethod.GET)
  @ResponseBody
  public void getDataset(HttpServletResponse httpServletResponse, @PathVariable String datasetUUID) throws IOException, URISyntaxException {
    logger.trace("retrieving head dataset");
    getVersionedDataset(httpServletResponse, datasetUUID, null);
  }

  @RequestMapping(value = "/{datasetUuid}/query", method = RequestMethod.GET)
  @ResponseBody
  public void executeHeadQuery(HttpServletResponse httpServletResponse,
                               @RequestHeader(value = "Accept") String acceptHeaderValue,
                               @RequestParam(value = "query") String query,
                               @PathVariable String datasetUuid) throws URISyntaxException, IOException {
    logger.trace("non versioned query starting");
    executeVersionedQuery(httpServletResponse, acceptHeaderValue, query, datasetUuid, null);
  }

  @RequestMapping(value = "/{datasetUuid}/versions/{versionUuid}/query", method = RequestMethod.GET)
  @ResponseBody
  public void executeVersionedQuery(HttpServletResponse httpServletResponse,
                                    @RequestHeader(value = "Accept") String acceptHeaderValue,
                                    @RequestParam(value = "query") String query,
                                    @PathVariable String datasetUuid, @PathVariable String versionUuid) throws URISyntaxException, IOException {
    logger.trace("executing gertseki query");
    URI trialverseDatasetUri = new URI(Namespaces.DATASET_NAMESPACE + datasetUuid);
    byte[] response = datasetReadRepository.executeQuery(query, trialverseDatasetUri, versionUuid, acceptHeaderValue);
    httpServletResponse.setStatus(HttpServletResponse.SC_OK);
    httpServletResponse.setHeader("Content-Type", acceptHeaderValue);
    trialverseIOUtilsService.writeContentToServletResponse(response, httpServletResponse);
  }

  @RequestMapping(value = "/{datasetUUID}/versions", method = RequestMethod.GET)
  @ResponseBody
  public void queryHistory(HttpServletResponse httpServletResponse, @PathVariable String datasetUUID) throws URISyntaxException, IOException {
    logger.trace("executing queryHistory");
    URI trialverseDatasetUri = new URI(Namespaces.DATASET_NAMESPACE + datasetUUID);
    byte[] response = datasetReadRepository.getHistory(trialverseDatasetUri);
    httpServletResponse.setStatus(HttpServletResponse.SC_OK);
    httpServletResponse.setHeader("Content-Type", RDFLanguages.JSONLD.getContentType().getContentType());
    trialverseIOUtilsService.writeContentToServletResponse(response, httpServletResponse);
  }

  @RequestMapping(value = "/{datasetUUID}/versions/{versionUuid}", method = RequestMethod.GET)
  @ResponseBody
  public void getVersionedDataset(HttpServletResponse httpServletResponse, @PathVariable String datasetUUID, @PathVariable String versionUuid) throws URISyntaxException {
    logger.trace("retrieving versioned dataset: {}", versionUuid);
    URI trialverseDatasetUri = new URI(Namespaces.DATASET_NAMESPACE + datasetUUID);
    Model datasetModel = datasetReadRepository.getVersionedDataset(trialverseDatasetUri, versionUuid);
    httpServletResponse.setStatus(HttpServletResponse.SC_OK);
    httpServletResponse.setHeader("Content-Type", RDFLanguages.TURTLE.getContentType().getContentType());
    trialverseIOUtilsService.writeModelToServletResponse(datasetModel, httpServletResponse);
  }
}

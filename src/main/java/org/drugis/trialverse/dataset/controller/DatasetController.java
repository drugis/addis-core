package org.drugis.trialverse.dataset.controller;

import org.apache.http.HttpException;
import org.apache.http.client.HttpClient;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFLanguages;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.trialverse.dataset.controller.command.DatasetCommand;
import org.drugis.trialverse.dataset.exception.CreateDatasetException;
import org.drugis.trialverse.dataset.exception.RevisionNotFoundException;
import org.drugis.trialverse.dataset.model.Dataset;
import org.drugis.trialverse.dataset.model.VersionNode;
import org.drugis.trialverse.dataset.repository.DatasetReadRepository;
import org.drugis.trialverse.dataset.repository.DatasetWriteRepository;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.dataset.service.DatasetService;
import org.drugis.trialverse.dataset.service.HistoryService;
import org.drugis.trialverse.security.TrialversePrincipal;
import org.drugis.trialverse.util.Namespaces;
import org.drugis.trialverse.util.WebConstants;
import org.drugis.trialverse.util.controller.AbstractTrialverseController;
import org.drugis.trialverse.util.service.TrialverseIOUtilsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.List;

/**
 * Created by connor on 6-11-14.
 */
@Controller
@RequestMapping(value = "/users/{userId}/datasets")
public class DatasetController extends AbstractTrialverseController {

  private final static Logger logger = LoggerFactory.getLogger(DatasetController.class);

  @Inject
  private DatasetWriteRepository datasetWriteRepository;

  @Inject
  private DatasetReadRepository datasetReadRepository;

  @Inject
  private HistoryService historyService;

  @Inject
  private TrialverseIOUtilsService trialverseIOUtilsService;

  @Inject
  private VersionMappingRepository versionMappingRepository;

  @Inject
  private AccountRepository accountRepository;

  @Inject
  private DatasetService datasetService;

  @Inject
  private HttpClient httpClient;

  @RequestMapping(method = RequestMethod.POST)
  @ResponseBody
  public void createDataset(HttpServletResponse response, Principal currentUser,
                            @RequestBody DatasetCommand datasetCommand, @PathVariable Integer userId)
          throws URISyntaxException, CreateDatasetException, HttpException {
    logger.trace("createDataset");
    TrialversePrincipal trialversePrincipal = new TrialversePrincipal(currentUser);
    Account user = accountRepository.findAccountByUsername(trialversePrincipal.getUserName());
    if (user != null && userId.equals(user.getId())) {
      URI datasetUri = datasetWriteRepository.createDataset(datasetCommand.getTitle(),
              datasetCommand.getDescription(), trialversePrincipal);
      response.setStatus(HttpServletResponse.SC_CREATED);
      response.setHeader("Location", datasetUri.toString());
    } else {
      logger.error("attempted to created database for user that is not the login-user ");
      response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
  }

  @RequestMapping(method = RequestMethod.GET, headers = WebConstants.ACCEPT_TURTLE_HEADER)
  @ResponseBody
  public void queryDatasetsGraphsByUser(HttpServletResponse httpServletResponse,
                                        @PathVariable Integer userId) {
    logger.trace("retrieving datasets");
    Account user = accountRepository.findAccountById(userId);
    httpServletResponse.setHeader("Content-Type", RDFLanguages.TURTLE.getContentType().getContentType());
    Model model = datasetReadRepository.queryDatasets(user);
    if (model != null) {
      httpServletResponse.setStatus(HttpStatus.OK.value());
    } else {
      httpServletResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
    trialverseIOUtilsService.writeModelToServletResponse(model, httpServletResponse);
  }

  @RequestMapping(method = RequestMethod.GET,
          headers = WebConstants.ACCEPT_JSON_HEADER,
          produces = WebConstants.APPLICATION_JSON_UTF8_VALUE)
  @ResponseBody
  public List<Dataset> queryDatasetsByUser(@PathVariable Integer userId) {
    logger.trace("retrieving datasets");
    Account user = accountRepository.findAccountById(userId);
    return datasetService.findDatasets(user);
  }


  @RequestMapping(value = "/{datasetUUID}", method = RequestMethod.GET)
  @ResponseBody
  public void getDataset(HttpServletResponse httpServletResponse, @PathVariable String datasetUUID) throws IOException, URISyntaxException {
    logger.trace("retrieving head dataset");
    getVersionedDatasetAsTurtle(httpServletResponse, datasetUUID, null);
  }

  @RequestMapping(value = "/{datasetUUID}", method = RequestMethod.GET, headers = WebConstants.ACCEPT_JSON_HEADER)
  @ResponseBody
  public void getDatasetAsJson(HttpServletResponse httpServletResponse, @PathVariable String datasetUUID) throws IOException, URISyntaxException {
    logger.trace("retrieving head dataset");
    getVersionedDatasetAsJson(httpServletResponse, datasetUUID, null);
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
  public List<VersionNode> queryHistory(HttpServletResponse httpServletResponse, @PathVariable String datasetUUID) throws URISyntaxException, IOException, RevisionNotFoundException {
    logger.trace("executing queryHistory");
    URI trialverseDatasetUri = new URI(Namespaces.DATASET_NAMESPACE + datasetUUID);
    List<VersionNode> history = historyService.createHistory(trialverseDatasetUri);
    httpServletResponse.setStatus(HttpServletResponse.SC_OK);
    return history;
  }

  @RequestMapping(value = "/{datasetUUID}/versions/{versionUuid}", method = RequestMethod.GET)
  @ResponseBody
  public void getVersionedDatasetAsTurtle(HttpServletResponse httpServletResponse, @PathVariable String datasetUUID, @PathVariable String versionUuid) throws URISyntaxException {
    logger.trace("retrieving versioned dataset: {}", versionUuid);
    Model model = getVersionedDatasetModel(datasetUUID, versionUuid);
    httpServletResponse.setStatus(HttpServletResponse.SC_OK);
    httpServletResponse.setHeader("Content-Type", RDFLanguages.TURTLE.getContentType().getContentType());
    trialverseIOUtilsService.writeModelToServletResponse(model, httpServletResponse);
  }

  @RequestMapping(value = "/{datasetUUID}/versions/{versionUuid}", method = RequestMethod.GET, headers = WebConstants.ACCEPT_JSON_HEADER)
  @ResponseBody
  public void getVersionedDatasetAsJson(HttpServletResponse httpServletResponse, @PathVariable String datasetUUID, @PathVariable String versionUuid) throws URISyntaxException {
    logger.trace("retrieving versioned dataset: {}", versionUuid);
    Model model = getVersionedDatasetModel(datasetUUID, versionUuid);
    httpServletResponse.setStatus(HttpServletResponse.SC_OK);
    httpServletResponse.setHeader("Content-Type", RDFLanguages.JSONLD.getContentType().getContentType());
    trialverseIOUtilsService.writeModelToServletResponseJson(model, httpServletResponse);
  }

  public Model getVersionedDatasetModel(String datasetUUID, String versionUuid) throws URISyntaxException {
    URI trialverseDatasetUri = new URI(Namespaces.DATASET_NAMESPACE + datasetUUID);
    return datasetReadRepository.getVersionedDataset(trialverseDatasetUri, versionUuid);
  }
}

package org.drugis.trialverse.dataset.controller;

import com.hp.hpl.jena.rdf.model.Model;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;

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
import org.slf4j.Marker;
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
@RequestMapping(value = "/datasets")
public class DatasetController extends AbstractTrialverseController {

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

  private final static String JSON_TYPE = "application/json; charset=UTF-8";

  Logger logger = LoggerFactory.getLogger(getClass());

  @RequestMapping(method = RequestMethod.POST)
  @ResponseBody
  public void createDataset(HttpServletRequest request, HttpServletResponse response, Principal currentUser, @RequestBody DatasetCommand datasetCommand) throws URISyntaxException, CreateDatasetException, HttpException {
    Account currentUserAccount = accountRepository.findAccountByUsername(currentUser.getName());
    URI datasetUri = datasetWriteRepository.createDataset(datasetCommand.getTitle(), datasetCommand.getDescription(), currentUserAccount);
    response.setStatus(HttpServletResponse.SC_CREATED);
    response.setHeader("Location", datasetUri.toString());
  }

  @RequestMapping(method = RequestMethod.GET)
  @ResponseBody
  public void queryDatasets(HttpServletResponse httpServletResponse, Principal currentUser) {
    logger.info("retrieving datasets");
    Account currentUserAccount = accountRepository.findAccountByUsername(currentUser.getName());
    httpServletResponse.setHeader("Content-Type", RDFLanguages.TURTLE.getContentType().getContentType());
    Model model = datasetReadRepository.queryDatasets(currentUserAccount);
    if (model != null) {
      httpServletResponse.setStatus(HttpStatus.OK.value());
    } else {
      httpServletResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
    trialverseIOUtilsService.writeModelToServletResponse(model, httpServletResponse);
    logger.error("datasets retrieved");
  }

  @RequestMapping(value = "/{datasetUUID}", method = RequestMethod.GET)
  @ResponseBody
  public void getDataset(HttpServletResponse httpServletResponse, @PathVariable String datasetUUID) throws IOException, URISyntaxException {
    logger.info("retrieving head dataset");
    getVersionedDataset(httpServletResponse, datasetUUID, null);
  }

  @RequestMapping(value = "/{datasetUuid}/query", method = RequestMethod.GET)
  @ResponseBody
  public void executeHeadQuery(HttpServletResponse httpServletResponse,
                               @RequestHeader(value = "Accept") String acceptHeaderValue,
                               @RequestParam(value = "query") String query,
                               @PathVariable String datasetUuid) throws URISyntaxException, IOException {
    logger.error("non versioned query starting");
    executeVersionedQuery(httpServletResponse, acceptHeaderValue, query, datasetUuid, null);
  }

  @RequestMapping(value = "/{datasetUuid}/versions/{versionUuid}/query", method = RequestMethod.GET)
  @ResponseBody
  public void executeVersionedQuery(HttpServletResponse httpServletResponse,
                                    @RequestHeader(value = "Accept") String acceptHeaderValue,
                                    @RequestParam(value = "query") String query,
                                    @PathVariable String datasetUuid, @PathVariable String versionUuid) throws URISyntaxException, IOException {
    URI trialverseDatasetUri = new URI(Namespaces.DATASET_NAMESPACE + datasetUuid);

    logger.error("executing gertseki query");
    HttpResponse response = datasetReadRepository.executeQuery(query, trialverseDatasetUri, versionUuid, acceptHeaderValue);

    httpServletResponse.setStatus(HttpServletResponse.SC_OK);
    httpServletResponse.setHeader("Content-Type", response.getFirstHeader("Content-Type").getValue());
    trialverseIOUtilsService.writeResponseContentToServletResponse(response, httpServletResponse);
    logger.info("query complete");
  }

  @RequestMapping(value = "/{datasetUUID}/versions", method = RequestMethod.GET)
  @ResponseBody
  public void queryHistory(HttpServletResponse httpServletResponse, @PathVariable String datasetUUID) throws URISyntaxException, IOException {
    URI trialverseDatasetUri = new URI(Namespaces.DATASET_NAMESPACE + datasetUUID);
    HttpResponse response = datasetReadRepository.getHistory(trialverseDatasetUri);
    httpServletResponse.setStatus(HttpServletResponse.SC_OK);
    httpServletResponse.setHeader("Content-Type", RDFLanguages.JSONLD.getContentType().getContentType());
    trialverseIOUtilsService.writeResponseContentToServletResponse(response, httpServletResponse);
  }

  @RequestMapping(value = "/{datasetUUID}/versions/{versionUuid}", method = RequestMethod.GET)
  @ResponseBody
  public void getVersionedDataset(HttpServletResponse httpServletResponse, @PathVariable String datasetUUID, @PathVariable String versionUuid) throws URISyntaxException {
    logger.info("retrieving versioned dataset: {}", versionUuid);
    URI trialverseDatasetUri = new URI(Namespaces.DATASET_NAMESPACE + datasetUUID);
    Model datasetModel = datasetReadRepository.getVersionedDataset(trialverseDatasetUri, versionUuid);
    httpServletResponse.setStatus(HttpServletResponse.SC_OK);
    httpServletResponse.setHeader("Content-Type", RDFLanguages.TURTLE.getContentType().getContentType());
    trialverseIOUtilsService.writeModelToServletResponse(datasetModel, httpServletResponse);
    logger.info("Dataset retrieved");
  }
}

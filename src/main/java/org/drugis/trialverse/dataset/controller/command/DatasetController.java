package org.drugis.trialverse.dataset.controller.command;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.WebContent;
import org.drugis.addis.base.AbstractAddisCoreController;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.util.WebConstants;
import org.drugis.trialverse.dataset.controller.DatasetArchiveCommand;
import org.drugis.trialverse.dataset.exception.CreateDatasetException;
import org.drugis.trialverse.dataset.exception.EditDatasetException;
import org.drugis.trialverse.dataset.exception.RevisionNotFoundException;
import org.drugis.trialverse.dataset.factory.JenaFactory;
import org.drugis.trialverse.dataset.model.Dataset;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.model.VersionNode;
import org.drugis.trialverse.dataset.repository.DatasetReadRepository;
import org.drugis.trialverse.dataset.repository.DatasetWriteRepository;
import org.drugis.trialverse.dataset.repository.FeaturedDatasetRepository;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.dataset.service.DatasetService;
import org.drugis.trialverse.dataset.service.HistoryService;
import org.drugis.trialverse.security.TrialversePrincipal;
import org.drugis.trialverse.util.Namespaces;
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
import java.util.List;

@Controller
@RequestMapping(value = "/users/{userId}/datasets")
public class DatasetController extends AbstractAddisCoreController {

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
  private FeaturedDatasetRepository featuredDatasetRepository;

  @RequestMapping(method = RequestMethod.POST)
  @ResponseBody
  public URI createDataset(
          HttpServletResponse response,
          Principal currentUser,
          @RequestBody DatasetCommand datasetCommand,
          @PathVariable Integer userId
  ) throws URISyntaxException, CreateDatasetException, MethodNotAllowedException {
    logger.trace("createDataset");
    datasetService.checkDatasetOwner(userId, currentUser);
    TrialversePrincipal trialversePrincipal = new TrialversePrincipal(currentUser);
    URI datasetUri = datasetWriteRepository.createDataset(datasetCommand.getTitle(),
            datasetCommand.getDescription(), trialversePrincipal);
    response.setStatus(HttpServletResponse.SC_CREATED);
    response.setHeader("Location", datasetUri.toString());
    return datasetUri;
  }

  @RequestMapping(path = "/{datasetUuid}", method = RequestMethod.POST,
          consumes = WebContent.contentTypeJSON)
  public void editDataset(
          HttpServletResponse response,
          Principal currentUser,
          @RequestBody DatasetCommand datasetCommand,
          @PathVariable Integer userId,
          @PathVariable String datasetUuid
  ) throws URISyntaxException, EditDatasetException, MethodNotAllowedException {
    datasetService.checkDatasetOwner(userId, currentUser);
    URI datasetUri = URI.create(Namespaces.DATASET_NAMESPACE + datasetUuid);
    VersionMapping mapping = versionMappingRepository.getVersionMappingByDatasetUrl(datasetUri);
    TrialversePrincipal trialversePrincipal = new TrialversePrincipal(currentUser);
    String newVersion = datasetWriteRepository.editDataset(trialversePrincipal, mapping, datasetCommand.getTitle(), datasetCommand.getDescription());
    response.setHeader(WebConstants.X_EVENT_SOURCE_VERSION, newVersion);
  }

  @RequestMapping(value = "/{datasetUuid}", method = RequestMethod.POST, consumes = WebConstants.TRIG)
  @ResponseBody
  public void createDatasetWithContent(
          HttpServletRequest request,
          HttpServletResponse response,
          Principal currentUser,
          @PathVariable Integer userId,
          @PathVariable String datasetUuid,
          @RequestParam(WebConstants.COMMIT_TITLE_PARAM) String commitTitle,
          @RequestParam(value = WebConstants.COMMIT_DESCRIPTION_PARAM, required = false) String commitDescription
  ) throws URISyntaxException, CreateDatasetException, IOException, MethodNotAllowedException {
    datasetService.checkDatasetOwner(userId, currentUser);
    logger.trace("createDatasetWithContent");
    TrialversePrincipal trialversePrincipal = new TrialversePrincipal(currentUser);
    URI datasetUri = datasetWriteRepository
            .createOrUpdateDatasetWithContent(
                    request.getInputStream(),
                    WebConstants.TRIG,
                    JenaFactory.DATASET + datasetUuid,
                    trialversePrincipal,
                    commitTitle,
                    commitDescription
            );
    response.setStatus(HttpServletResponse.SC_CREATED);
    response.setHeader("Location", datasetUri.toString());
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


  @RequestMapping(value = "/{datasetUuid}", method = RequestMethod.GET)
  @ResponseBody
  public void getDataset(HttpServletResponse httpServletResponse, @PathVariable String datasetUuid) throws URISyntaxException {
    logger.trace("retrieving head dataset");
    getVersionedDatasetAsTurtle(httpServletResponse, datasetUuid, null);
  }

  @RequestMapping(value = "/{datasetUuid}", method = RequestMethod.GET, headers = WebConstants.ACCEPT_JSON_HEADER)
  @ResponseBody
  public void getDatasetAsJson(HttpServletResponse httpServletResponse, @PathVariable String datasetUuid) throws URISyntaxException {
    logger.trace("retrieving head dataset");
    getVersionedDatasetAsJson(httpServletResponse, datasetUuid, null);
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
  public void executeVersionedQuery(
          HttpServletResponse httpServletResponse,
          @RequestHeader(value = "Accept") String acceptHeaderValue,
          @RequestParam(value = "query") String query,
          @PathVariable String datasetUuid,
          @PathVariable String versionUuid
  ) throws URISyntaxException, IOException {
    logger.trace("executing gertseki query");
    URI trialverseDatasetUri = new URI(Namespaces.DATASET_NAMESPACE + datasetUuid);
    byte[] response = datasetReadRepository
            .executeQuery(query, trialverseDatasetUri, WebConstants.buildVersionUri(versionUuid), acceptHeaderValue);
    httpServletResponse.setStatus(HttpServletResponse.SC_OK);
    httpServletResponse.setHeader("Content-Type", acceptHeaderValue);
    trialverseIOUtilsService.writeContentToServletResponse(response, httpServletResponse);
  }

  @RequestMapping(value = "/{datasetUuid}/history", method = RequestMethod.GET)
  @ResponseBody
  public List<VersionNode> queryHistory(
          HttpServletResponse httpServletResponse,
          @PathVariable String datasetUuid
  ) throws URISyntaxException, IOException, RevisionNotFoundException {
    logger.trace("executing queryHistory");
    URI trialverseDatasetUri = new URI(Namespaces.DATASET_NAMESPACE + datasetUuid);
    List<VersionNode> history = historyService.createHistory(trialverseDatasetUri);
    httpServletResponse.setStatus(HttpServletResponse.SC_OK);
    return history;
  }

  @RequestMapping(value = "/{datasetUuid}/history/{versionUuid}", method = RequestMethod.GET)
  @ResponseBody
  public VersionNode getVersionInfo(@PathVariable String datasetUuid, @PathVariable String versionUuid) throws IOException, URISyntaxException {
    URI trialverseDatasetUri = URI.create(Namespaces.DATASET_NAMESPACE + datasetUuid);
    URI versionUri = URI.create(WebConstants.getVersionBaseUri() + versionUuid);
    logger.info("retrieving version info for " + trialverseDatasetUri.toString() + " / " + versionUri.toString());
    return historyService.getVersionInfo(trialverseDatasetUri, versionUri);
  }

  @RequestMapping(value = "/{datasetUuid}/versions/{versionUuid}", method = RequestMethod.GET)
  @ResponseBody
  public void getVersionedDatasetAsTurtle(HttpServletResponse httpServletResponse, @PathVariable String datasetUuid, @PathVariable String versionUuid) throws URISyntaxException {
    logger.trace("retrieving versioned dataset: {}", versionUuid);
    Model model = getVersionedDatasetModel(datasetUuid, versionUuid);
    httpServletResponse.setStatus(HttpServletResponse.SC_OK);
    httpServletResponse.setHeader("Content-Type", RDFLanguages.TURTLE.getContentType().getContentType());
    trialverseIOUtilsService.writeModelToServletResponse(model, httpServletResponse);
  }

  @RequestMapping(value = "/{datasetUuid}/versions/{versionUuid}", method = RequestMethod.GET, headers = WebConstants.ACCEPT_JSON_HEADER)
  @ResponseBody
  public void getVersionedDatasetAsJson(HttpServletResponse httpServletResponse, @PathVariable String datasetUuid, @PathVariable String versionUuid) throws URISyntaxException {
    logger.trace("retrieving versioned dataset: {}", versionUuid);
    Model model = getVersionedDatasetModel(datasetUuid, versionUuid);
    httpServletResponse.setStatus(HttpServletResponse.SC_OK);
    httpServletResponse.setHeader("Content-Type", RDFLanguages.JSONLD.getContentType().getContentType());
    trialverseIOUtilsService.writeModelToServletResponseJson(model, httpServletResponse);
  }

  private Model getVersionedDatasetModel(String datasetUuid, String versionUuid) throws URISyntaxException {
    URI trialverseDatasetUri = new URI(Namespaces.DATASET_NAMESPACE + datasetUuid);
    return datasetReadRepository.getVersionedDataset(trialverseDatasetUri, versionUuid);
  }

  @RequestMapping(value = "/{datasetUuid}/setArchivedStatus", method = RequestMethod.POST)
  @ResponseBody
  public void setArchivedStatus(
          HttpServletResponse response,
          @PathVariable String datasetUuid,
          @PathVariable Integer userId,
          Principal currentUser,
          @RequestBody DatasetArchiveCommand archiveCommand
  ) throws MethodNotAllowedException {
    datasetService.checkDatasetOwner(userId, currentUser);
    URI datasetUri = URI.create(Namespaces.DATASET_NAMESPACE + datasetUuid);
    versionMappingRepository.setArchivedStatus(datasetUri, archiveCommand.getArchived());
    response.setStatus(HttpServletResponse.SC_OK);

  }
}

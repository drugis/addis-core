package org.drugis.trialverse.graph.controller;

import org.apache.http.Header;
import org.apache.jena.riot.RDFLanguages;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.util.WebConstants;
import org.drugis.trialverse.dataset.exception.RevisionNotFoundException;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.repository.DatasetReadRepository;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.exception.MethodNotAllowedException;
import org.drugis.trialverse.graph.exception.ReadGraphException;
import org.drugis.trialverse.graph.exception.UpdateGraphException;
import org.drugis.trialverse.graph.repository.GraphReadRepository;
import org.drugis.trialverse.graph.repository.GraphWriteRepository;
import org.drugis.trialverse.graph.service.GraphService;
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
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;

/**
 * Created by daan on 19-11-14.
 */
@Controller
@RequestMapping(value = "/users/{userUid}/datasets/{datasetUuid}")
public class GraphController extends AbstractTrialverseController {

  @Inject
  private GraphService graphService;

  @Inject
  private GraphReadRepository graphReadRepository;

  @Inject
  private GraphWriteRepository graphWriteRepository;

  @Inject
  private DatasetReadRepository datasetReadRepository;

  @Inject
  private AccountRepository accountRepository;

  @Inject
  private TrialverseIOUtilsService trialverseIOUtilsService;

  @Inject
  private VersionMappingRepository versionMappingRepository;



  Logger logger = LoggerFactory.getLogger(getClass());

  @RequestMapping(value = "/versions/{versionUuid}/graphs/{graphUuid}", method = RequestMethod.GET, produces = WebConstants.TURTLE)
  @ResponseBody
  public void getGraphTurtle(HttpServletResponse httpServletResponse, @PathVariable String datasetUuid,
                       @PathVariable String versionUuid, @PathVariable String graphUuid) throws URISyntaxException, IOException, ReadGraphException {
    logger.trace("get graph");
    VersionMapping versionMapping = versionMappingRepository.getVersionMappingByDatasetUrl(new URI(Namespaces.DATASET_NAMESPACE + datasetUuid));
    byte[] responseContent = graphReadRepository.getGraph(versionMapping.getVersionedDatasetUrl(), versionUuid, graphUuid, WebConstants.TURTLE);
    httpServletResponse.setStatus(HttpServletResponse.SC_OK);
    httpServletResponse.setHeader("Content-Type", RDFLanguages.TURTLE.getContentType().getContentType());
    trialverseIOUtilsService.writeContentToServletResponse(responseContent, httpServletResponse);
  }

  @RequestMapping(value = "/versions/{versionUuid}/graphs/{graphUuid}", method = RequestMethod.GET, produces = WebConstants.JSON_LD)
  @ResponseBody
  public void getGraphJsonLD(HttpServletResponse httpServletResponse, @PathVariable String datasetUuid,
                       @PathVariable String versionUuid, @PathVariable String graphUuid) throws URISyntaxException, IOException, ReadGraphException {
    logger.trace("get version graph");
    VersionMapping versionMapping = versionMappingRepository.getVersionMappingByDatasetUrl(new URI(Namespaces.DATASET_NAMESPACE + datasetUuid));
    byte[] responseContent = graphReadRepository.getGraph(versionMapping.getVersionedDatasetUrl(), versionUuid, graphUuid, WebConstants.JSON_LD);
    httpServletResponse.setStatus(HttpServletResponse.SC_OK);
    httpServletResponse.setHeader("Content-Type", WebConstants.JSON_LD);
    trialverseIOUtilsService.writeContentToServletResponse(responseContent, httpServletResponse);
  }

  @RequestMapping(value = "/graphs/{graphUuid}", method = RequestMethod.GET, produces = WebConstants.JSON_LD)
  @ResponseBody
  public void getGraphHeadVersionJsonLD(HttpServletResponse httpServletResponse, @PathVariable String datasetUuid, @PathVariable String graphUuid) throws URISyntaxException, IOException, ReadGraphException {
    logger.trace("get head graph");
    VersionMapping versionMapping = versionMappingRepository.getVersionMappingByDatasetUrl(new URI(Namespaces.DATASET_NAMESPACE + datasetUuid));
    byte[] responseContent = graphReadRepository.getGraph(versionMapping.getVersionedDatasetUrl(), null, graphUuid, WebConstants.JSON_LD);
    httpServletResponse.setStatus(HttpServletResponse.SC_OK);
    httpServletResponse.setHeader("Content-Type", WebConstants.JSON_LD);
    trialverseIOUtilsService.writeContentToServletResponse(responseContent, httpServletResponse);
  }


  @RequestMapping(value = "/graphs/{graphUuid}",
          method = RequestMethod.PUT, consumes = WebConstants.JSON_LD)
  public void setJsonGraph(HttpServletRequest request, HttpServletResponse trialverseResponse, Principal currentUser,
                           @RequestParam(WebConstants.COMMIT_TITLE_PARAM) String commitTitle,
                           @RequestParam(value = WebConstants.COMMIT_DESCRIPTION_PARAM, required = false) String commitDescription,
                           @PathVariable String datasetUuid, @PathVariable String graphUuid)
          throws IOException, MethodNotAllowedException, URISyntaxException, UpdateGraphException {
    logger.trace("set graph");
    URI trialverseDatasetUri = new URI(Namespaces.DATASET_NAMESPACE + datasetUuid);
    if (datasetReadRepository.isOwner(trialverseDatasetUri, currentUser)) {
      InputStream turtleStream = graphService.jsonGraphInputStreamToTurtleInputStream(request.getInputStream());
      createGraph(trialverseResponse, commitTitle, commitDescription, datasetUuid, graphUuid, turtleStream);
    } else {
      throw new MethodNotAllowedException();
    }
  }

  private void createGraph(HttpServletResponse trialverseResponse,
                           String commitTitle,
                           String commitDescription,
                           String datasetUuid,
                           String graphUuid, InputStream graph) throws IOException, UpdateGraphException, URISyntaxException {
    Header versionHeader = graphWriteRepository.updateGraph(new URI(Namespaces.DATASET_NAMESPACE + datasetUuid), graphUuid, graph, commitTitle, commitDescription);
    trialverseResponse.setHeader(WebConstants.X_EVENT_SOURCE_VERSION, versionHeader.getValue());
    trialverseResponse.setStatus(HttpStatus.OK.value());
  }

  @RequestMapping(value = "/graphs/{graphUuid}", method = RequestMethod.PUT, consumes = WebConstants.TURTLE)
  public void setTurtleGraph(HttpServletRequest request, HttpServletResponse trialverseResponse, Principal currentUser,
                             @RequestParam(WebConstants.COMMIT_TITLE_PARAM) String commitTitle,
                             @RequestParam(value = WebConstants.COMMIT_DESCRIPTION_PARAM, required = false) String commitDescription,
                             @PathVariable String datasetUuid, @PathVariable String graphUuid)
          throws IOException, MethodNotAllowedException, URISyntaxException, UpdateGraphException {
    logger.trace("set graph");
    URI trialverseDatasetUri = new URI(Namespaces.DATASET_NAMESPACE + datasetUuid);
    if (datasetReadRepository.isOwner(trialverseDatasetUri, currentUser)) {
      createGraph(trialverseResponse, commitTitle, commitDescription, datasetUuid, graphUuid, request.getInputStream());
    } else {
      throw new MethodNotAllowedException();
    }
  }


  @RequestMapping(value = "/graphs/{graphUuid}", method = RequestMethod.PUT, params = {WebConstants.COPY_OF_QUERY_PARAM})
  public void copyGraph(HttpServletRequest request, HttpServletResponse trialverseResponse, Principal currentUser,
                        @RequestParam(WebConstants.COPY_OF_QUERY_PARAM) String copyOfUri,
                        @PathVariable String datasetUuid, @PathVariable String graphUuid)
          throws IOException, MethodNotAllowedException, URISyntaxException, UpdateGraphException, RevisionNotFoundException {
    logger.trace("copy graph");
    URI trialverseDatasetUri = new URI(Namespaces.DATASET_NAMESPACE + datasetUuid);
    if (datasetReadRepository.isOwner(trialverseDatasetUri, currentUser)) {
      URI targetGraphUri = graphService.buildGraphUri(graphUuid);
      URI newVersion = graphService.copy(trialverseDatasetUri, targetGraphUri, URI.create(copyOfUri));
      trialverseResponse.setHeader(WebConstants.X_EVENT_SOURCE_VERSION, newVersion.toString());
      trialverseResponse.setStatus(HttpStatus.OK.value());
    } else {
      throw new MethodNotAllowedException();
    }
  }
}

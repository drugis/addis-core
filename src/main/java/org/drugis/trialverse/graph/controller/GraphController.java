package org.drugis.trialverse.graph.controller;

import org.apache.http.Header;
import org.apache.jena.riot.RDFLanguages;
import org.drugis.addis.base.AbstractAddisCoreController;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.importer.service.ClinicalTrialsImportService;
import org.drugis.addis.importer.service.impl.ClinicalTrialsImportError;
import org.drugis.addis.util.WebConstants;
import org.drugis.trialverse.dataset.exception.RevisionNotFoundException;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.model.VersionNode;
import org.drugis.trialverse.dataset.repository.DatasetReadRepository;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.dataset.service.HistoryService;
import org.drugis.trialverse.exception.MethodNotAllowedException;
import org.drugis.trialverse.graph.exception.DeleteGraphException;
import org.drugis.trialverse.graph.exception.ReadGraphException;
import org.drugis.trialverse.graph.exception.UpdateGraphException;
import org.drugis.trialverse.graph.repository.GraphReadRepository;
import org.drugis.trialverse.graph.repository.GraphWriteRepository;
import org.drugis.trialverse.graph.service.GraphService;
import org.drugis.trialverse.util.Namespaces;
import org.drugis.trialverse.util.service.TrialverseIOUtilsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping(value = "/users/{userUid}/datasets/{datasetUuid}")
public class GraphController extends AbstractAddisCoreController {

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

    @Inject
    private ClinicalTrialsImportService clinicalTrialsImportService;

    @Inject
    private HistoryService historyService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @RequestMapping(value = "/versions/{versionUuid}/graphs/{graphUuid}", method = RequestMethod.GET, produces = WebConstants.TURTLE)
    @ResponseBody
    public void getGraphTurtle(HttpServletResponse httpServletResponse,
                               @PathVariable(value = "datasetUuid") String datasetUuid,
                               @PathVariable(value = "versionUuid") String versionUuid,
                               @PathVariable(value = "graphUuid") String graphUuid) throws URISyntaxException, IOException, ReadGraphException {
        logger.trace("get graph");
        VersionMapping versionMapping = versionMappingRepository.getVersionMappingByDatasetUrl(new URI(Namespaces.DATASET_NAMESPACE + datasetUuid));
        byte[] responseContent = graphReadRepository.getGraph(versionMapping.getVersionedDatasetUrl(), versionUuid, graphUuid, WebConstants.TURTLE);
        httpServletResponse.setStatus(HttpServletResponse.SC_OK);
        httpServletResponse.setHeader("Content-Type", RDFLanguages.TURTLE.getContentType().getContentTypeStr());
        trialverseIOUtilsService.writeContentToServletResponse(responseContent, httpServletResponse);
    }

    @RequestMapping(value = "/versions/{versionUuid}/graphs/{graphUuid}", method = RequestMethod.GET, produces = WebConstants.JSON_LD)
    @ResponseBody
    public void getGraphJsonLD(HttpServletResponse httpServletResponse, 
                               @PathVariable(value="datasetUuid") String datasetUuid,
                               @PathVariable(value="versionUuid") String versionUuid, 
                               @PathVariable(value="graphUuid") String graphUuid) throws URISyntaxException, IOException, ReadGraphException {
        logger.trace("get version graph");
        getGraphJson(httpServletResponse, datasetUuid, graphUuid, versionUuid);
    }

    @RequestMapping(value = "/graphs/{graphUuid}", method = RequestMethod.GET, produces = WebConstants.JSON_LD)
    @ResponseBody
    public void getGraphHeadVersionJsonLD(HttpServletResponse httpServletResponse, @PathVariable(value="datasetUuid") String datasetUuid, @PathVariable(value="graphUuid") String graphUuid) throws URISyntaxException, IOException, ReadGraphException {
        logger.trace("get head graph");
        VersionMapping versionMapping = versionMappingRepository.getVersionMappingByDatasetUrl(new URI(Namespaces.DATASET_NAMESPACE + datasetUuid));
        byte[] responseContent = graphReadRepository.getGraph(versionMapping.getVersionedDatasetUrl(), null, graphUuid, WebConstants.JSON_LD);
        if (new String(responseContent).equals("{ }\n")) {
            httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
            httpServletResponse.setStatus(HttpServletResponse.SC_OK);
            httpServletResponse.setHeader("Content-Type", WebConstants.JSON_LD);
            trialverseIOUtilsService.writeContentToServletResponse(responseContent, httpServletResponse);
        }
    }

    @RequestMapping(value = "/graphs/{graphUuid}/concepts", method = RequestMethod.GET, produces = WebConstants.JSON_LD)
    @ResponseBody
    public void getHeadConceptsJson(HttpServletResponse httpServletResponse, @PathVariable(value="datasetUuid") String datasetUuid, @PathVariable(value="graphUuid") String graphUuid) throws URISyntaxException, IOException, ReadGraphException {
        logger.trace("get concepts graph");
        getGraphJson(httpServletResponse, datasetUuid, graphUuid, null);
    }

    @RequestMapping(value = "/versions/{versionUuid}/graphs/{graphUuid}/concepts", method = RequestMethod.GET, produces = WebConstants.JSON_LD)
    @ResponseBody
    public void getVersionedConceptsJson(
            HttpServletResponse httpServletResponse,
            @PathVariable(value="datasetUuid") String datasetUuid,
            @PathVariable(value="graphUuid") String graphUuid,
            @PathVariable(value="versionUuid") String versionUuid
    ) throws URISyntaxException, IOException, ReadGraphException {
        logger.trace("get concepts graph");
        getGraphJson(httpServletResponse, datasetUuid, graphUuid, versionUuid);
    }

    private void getGraphJson(HttpServletResponse httpServletResponse, @PathVariable(value="datasetUuid") String datasetUuid, @PathVariable(value="graphUuid") String graphUuid, @PathVariable(value="versionUuid") String versionUuid) throws URISyntaxException, IOException, ReadGraphException {
        VersionMapping versionMapping = versionMappingRepository.getVersionMappingByDatasetUrl(new URI(Namespaces.DATASET_NAMESPACE + datasetUuid));
        byte[] responseContent = graphReadRepository.getGraph(versionMapping.getVersionedDatasetUrl(), versionUuid, graphUuid, WebConstants.JSON_LD);
        httpServletResponse.setStatus(HttpServletResponse.SC_OK);
        httpServletResponse.setHeader("Content-Type", WebConstants.JSON_LD);
        trialverseIOUtilsService.writeContentToServletResponse(responseContent, httpServletResponse);
    }


    @RequestMapping(value = "/graphs/{graphUuid}/history", method = RequestMethod.GET)
    @ResponseBody
    public List<VersionNode> getGraphHistory(HttpServletResponse httpServletResponse,
                                             @PathVariable(value="datasetUuid") String datasetUuid,
                                             @PathVariable(value="graphUuid") String graphUuid) throws URISyntaxException, IOException, RevisionNotFoundException {
        URI trialverseDatasetUri = new URI(Namespaces.DATASET_NAMESPACE + datasetUuid);
        URI trialverseGraphUri = new URI(Namespaces.GRAPH_NAMESPACE + graphUuid);

        List<VersionNode> history = historyService.createHistory(trialverseDatasetUri, trialverseGraphUri);
        httpServletResponse.setStatus(HttpServletResponse.SC_OK);
        return history;
    }


    @RequestMapping(value = "/graphs/{graphUuid}", method = RequestMethod.DELETE)
    public void deleteGraph(HttpServletResponse httpServletResponse, Principal currentUser,
                            @PathVariable(value="datasetUuid") String datasetUuid,
                            @PathVariable(value="graphUuid") String graphUuid) throws URISyntaxException, DeleteGraphException {
        URI trialverseDatasetUri = new URI(Namespaces.DATASET_NAMESPACE + datasetUuid);
        if (datasetReadRepository.isOwner(trialverseDatasetUri, currentUser)) {
            VersionMapping versionMapping = versionMappingRepository.getVersionMappingByDatasetUrl(trialverseDatasetUri);
            Header versionHeader = graphWriteRepository.deleteGraph(versionMapping.getVersionedDatasetUri(), graphUuid);
            httpServletResponse.setHeader(WebConstants.X_EVENT_SOURCE_VERSION, versionHeader.getValue());
            httpServletResponse.setStatus(HttpStatus.OK.value());
        }
    }

    @RequestMapping(value = "/graphs/{graphUuid}", method = RequestMethod.PUT, consumes = WebConstants.JSON_LD)
    public void setJsonGraph(HttpServletRequest request, HttpServletResponse trialverseResponse, Principal currentUser,
                             @RequestParam(WebConstants.COMMIT_TITLE_PARAM) String commitTitle,
                             @RequestParam(value = WebConstants.COMMIT_DESCRIPTION_PARAM, required = false) String commitDescription,
                             @PathVariable(value="datasetUuid") String datasetUuid,
                             @PathVariable(value = "graphUuid") String graphUuid)
            throws IOException, MethodNotAllowedException, URISyntaxException, UpdateGraphException {
        logger.trace("set graph");
        URI trialverseDatasetUri = new URI(Namespaces.DATASET_NAMESPACE + datasetUuid);
        if (datasetReadRepository.isOwner(trialverseDatasetUri, currentUser)) {
            InputStream jsonldStream = graphService.jsonGraphInputStreamToTurtleInputStream(request.getInputStream());
            createGraph(trialverseResponse, commitTitle, commitDescription, datasetUuid, graphUuid, jsonldStream);
        } else {
            throw new MethodNotAllowedException();
        }
    }

    @RequestMapping(value = "/graphs/{graphUuid}/import/{importStudyRef}",
            method = RequestMethod.POST)
    public void importStudy(
            HttpServletResponse trialverseResponse,
            Principal currentUser,
            @PathVariable(value = "datasetUuid") String datasetUuid,
            @PathVariable(value = "graphUuid") String graphUuid,
            @PathVariable(value = "importStudyRef") String importStudyRef,
            @RequestParam(WebConstants.COMMIT_TITLE_PARAM) String commitTitle,
            @RequestParam(value = WebConstants.COMMIT_DESCRIPTION_PARAM, required = false) String commitDescription
    ) throws MethodNotAllowedException, ClinicalTrialsImportError, URISyntaxException {
        logger.trace("import graph");
        URI datasetUri = new URI(Namespaces.DATASET_NAMESPACE + datasetUuid);
        if (datasetReadRepository.isOwner(datasetUri, currentUser)) {
            VersionMapping mapping = versionMappingRepository.getVersionMappingByDatasetUrl(datasetUri);
            Header versionHeader = clinicalTrialsImportService.importStudy(commitTitle, commitDescription, mapping.getVersionedDatasetUrl(), graphUuid, importStudyRef);
            trialverseResponse.setHeader(WebConstants.X_EVENT_SOURCE_VERSION, versionHeader.getValue());
            trialverseResponse.setStatus(HttpStatus.OK.value());
        } else {
            throw new MethodNotAllowedException();
        }
    }

    @RequestMapping(value = "/graphs/import-eudract",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_XML_VALUE)
    public void importEudract(
            HttpServletRequest request,
            HttpServletResponse response,
            Principal currentUser,
            @PathVariable(value = "datasetUuid") String datasetUuid
    ) throws MethodNotAllowedException, URISyntaxException, IOException {
        logger.trace("import graph");
        URI datasetUri = new URI(Namespaces.DATASET_NAMESPACE + datasetUuid);
        if (datasetReadRepository.isOwner(datasetUri, currentUser)) {
            VersionMapping mapping = versionMappingRepository.getVersionMappingByDatasetUrl(datasetUri);
            Header versionHeader = clinicalTrialsImportService.importEudract(mapping.getVersionedDatasetUrl(),
                    UUID.randomUUID().toString(), request.getInputStream());
            response.setHeader(WebConstants.X_EVENT_SOURCE_VERSION, versionHeader.getValue());
            response.setStatus(HttpStatus.OK.value());
        } else {
            throw new MethodNotAllowedException();
        }
    }

    private void createGraph(
            HttpServletResponse trialverseResponse,
            String commitTitle,
            String commitDescription,
            String datasetUuid,
            String graphUuid,
            InputStream graph
    ) throws IOException, UpdateGraphException, URISyntaxException {
        URI versionedDatasetUri = versionMappingRepository.getVersionMappingByDatasetUrl(
                new URI(Namespaces.DATASET_NAMESPACE + datasetUuid)).getVersionedDatasetUri();
        Header versionHeader = graphWriteRepository.updateGraph(versionedDatasetUri, graphUuid, graph, commitTitle,
                commitDescription);
        trialverseResponse.setHeader(WebConstants.X_EVENT_SOURCE_VERSION, versionHeader.getValue());
        trialverseResponse.setStatus(HttpStatus.OK.value());
    }

    @RequestMapping(value = "/graphs/{graphUuid}", method = RequestMethod.PUT, consumes = WebConstants.TURTLE)
    public void setTurtleGraph(
            HttpServletRequest request, HttpServletResponse trialverseResponse, Principal currentUser,
            @RequestParam(value = WebConstants.COMMIT_TITLE_PARAM) String commitTitle,
            @RequestParam(value = WebConstants.COMMIT_DESCRIPTION_PARAM, required = false) String commitDescription,
            @PathVariable(value = "datasetUuid") String datasetUuid,
            @PathVariable(value = "graphUuid") String graphUuid
    ) throws IOException, MethodNotAllowedException, URISyntaxException, UpdateGraphException {
        logger.trace("set graph");
        URI trialverseDatasetUri = new URI(Namespaces.DATASET_NAMESPACE + datasetUuid);
        if (datasetReadRepository.isOwner(trialverseDatasetUri, currentUser)) {
            createGraph(trialverseResponse, commitTitle, commitDescription, datasetUuid, graphUuid, request.getInputStream());
        } else {
            throw new MethodNotAllowedException();
        }
    }


    @RequestMapping(value = "/graphs/{graphUuid}", method = RequestMethod.PUT, params = {WebConstants.COPY_OF_QUERY_PARAM})
    public void copyGraph(HttpServletResponse trialverseResponse, Principal currentUser,
                          @RequestParam(WebConstants.COPY_OF_QUERY_PARAM) String copyOfUri,
                          @PathVariable(value = "datasetUuid") String datasetUuid,
                          @PathVariable(value = "graphUuid") String graphUuid)
            throws IOException, MethodNotAllowedException, URISyntaxException, RevisionNotFoundException {
        logger.trace("copy graph");
        URI trialverseDatasetUri = new URI(Namespaces.DATASET_NAMESPACE + datasetUuid);
        if (datasetReadRepository.isOwner(trialverseDatasetUri, currentUser)) {
            URI targetGraphUri = graphService.buildGraphUri(graphUuid);
            VersionMapping targetDatasetMapping = versionMappingRepository.getVersionMappingByDatasetUrl(trialverseDatasetUri);
            URI newVersion = graphService.copy(targetDatasetMapping.getVersionedDatasetUri(), targetGraphUri, URI.create(copyOfUri));
            trialverseResponse.setHeader(WebConstants.X_EVENT_SOURCE_VERSION, newVersion.toString());
            trialverseResponse.setStatus(HttpStatus.OK.value());
        } else {
            throw new MethodNotAllowedException();
        }
    }
}

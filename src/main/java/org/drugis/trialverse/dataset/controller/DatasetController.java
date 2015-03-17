package org.drugis.trialverse.dataset.controller;

import com.hp.hpl.jena.rdf.model.Model;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.riot.RDFLanguages;
import org.drugis.trialverse.dataset.controller.command.DatasetCommand;
import org.drugis.trialverse.dataset.model.Dataset;
import org.drugis.trialverse.dataset.repository.DatasetReadRepository;
import org.drugis.trialverse.dataset.repository.DatasetWriteRepository;
import org.drugis.trialverse.exception.CreateDatasetException;
import org.drugis.trialverse.exception.MethodNotAllowedException;
import org.drugis.trialverse.security.Account;
import org.drugis.trialverse.security.repository.AccountRepository;
import org.drugis.trialverse.util.Namespaces;
import org.drugis.trialverse.util.WebConstants;
import org.drugis.trialverse.util.controller.AbstractTrialverseController;
import org.drugis.trialverse.util.service.TrialverseIOUtilsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

  private final static String JSON_TYPE =  "application/json; charset=UTF-8";

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
    Account currentUserAccount = accountRepository.findAccountByUsername(currentUser.getName());
    httpServletResponse.setHeader("Content-Type", RDFLanguages.TURTLE.getContentType().getContentType());
    Model model = datasetReadRepository.queryDatasets(currentUserAccount);
    if(model != null) {
        httpServletResponse.setStatus(HttpStatus.OK.value());
    } else {
        httpServletResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
    trialverseIOUtilsService.writeModelToServletResponse(model, httpServletResponse);
  }

  @RequestMapping(value = "/{datasetUUID}", method = RequestMethod.GET)
  @ResponseBody
  public void getDataset(HttpServletResponse httpServletResponse, @PathVariable String datasetUUID) throws IOException, URISyntaxException {
    URI trialverseDatasetUri = new URI(Namespaces.DATASET_NAMESPACE + datasetUUID);
    Model datasetModel = datasetReadRepository.getDataset(trialverseDatasetUri);
    httpServletResponse.setStatus(HttpServletResponse.SC_OK);
    httpServletResponse.setHeader("Content-Type", RDFLanguages.TURTLE.getContentType().getContentType());
    trialverseIOUtilsService.writeModelToServletResponse(datasetModel, httpServletResponse);
  }

  @RequestMapping(value = "/{datasetUUID}/studiesWithDetail", method = RequestMethod.GET)
  @ResponseBody
  public void queryStudiesWithDetail(HttpServletResponse httpServletResponse, @PathVariable String datasetUUID) throws URISyntaxException, IOException {
    URI trialverseDatasetUri = new URI(Namespaces.DATASET_NAMESPACE + datasetUUID);
    HttpResponse response = datasetReadRepository.queryStudiesWithDetail(trialverseDatasetUri);
    httpServletResponse.setStatus(HttpServletResponse.SC_OK);
    httpServletResponse.setHeader("Content-Type", JSON_TYPE);
    trialverseIOUtilsService.writeResponseContentToServletResponse(response, httpServletResponse);
  }


  @RequestMapping(value = "/{datasetUUID}", method = RequestMethod.POST)
  public void updateDataset(HttpServletRequest request, HttpServletResponse response, Principal currentUser,
                            @PathVariable String datasetUUID) throws IOException, MethodNotAllowedException, URISyntaxException {
    URI trialverseDatasetUri = new URI(Namespaces.DATASET_NAMESPACE + datasetUUID);
    if (datasetReadRepository.isOwner(trialverseDatasetUri, currentUser)) {
      HttpResponse jenaResponse = datasetWriteRepository.updateDataset(trialverseDatasetUri, request.getInputStream());
      response.setStatus(jenaResponse.getStatusLine().getStatusCode());
    } else {
      throw new MethodNotAllowedException();
    }
  }

}

package org.drugis.trialverse.dataset.repository.impl;


import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.protocol.HTTP;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.vocabulary.DCTerms;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.trialverse.dataset.factory.JenaFactory;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.repository.DatasetWriteRepository;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.dataset.exception.CreateDatasetException;
import org.drugis.trialverse.security.TrialversePrincipal;
import org.drugis.trialverse.util.WebConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by connor on 04/11/14.
 */

@Repository
public class DatasetWriteRepositoryImpl implements DatasetWriteRepository {

  public static final String PATH = "/datasets";
  public static final String INITIAL_COMMIT_MESSAGE = "Dataset created through Trialverse";

  @Inject
  private WebConstants webConstants;

  @Inject
  private RestTemplate restTemplate;

  @Inject
  private JenaFactory jenaFactory;

  @Inject
  private VersionMappingRepository versionMappingRepository;

  @Inject
  private AccountRepository accountRepository;

  private final static Logger logger = LoggerFactory.getLogger(DatasetWriteRepositoryImpl.class);

  @Override
  public URI createDataset(String title, String description, TrialversePrincipal owner) throws URISyntaxException, CreateDatasetException {
    HttpHeaders httpHeaders = new HttpHeaders();
    Account account = accountRepository.findAccountByUsername(owner.getUserName());
    if(owner.hasApiKey()) {
      httpHeaders.add(WebConstants.EVENT_SOURCE_CREATOR_HEADER,
              "https://trialverse.org/apikeys/" + owner.getApiKey().getId());
    } else {
      httpHeaders.add(WebConstants.EVENT_SOURCE_CREATOR_HEADER, "mailto:" + account.getEmail());
    }
    httpHeaders.add(WebConstants.EVENT_SOURCE_TITLE_HEADER, Base64.encodeBase64String(INITIAL_COMMIT_MESSAGE.getBytes()));
    httpHeaders.add(HTTP.CONTENT_TYPE, RDFLanguages.TURTLE.getContentType().getContentType());
    String datasetUri = jenaFactory.createDatasetURI();
    Model baseDatasetModel = buildDatasetBaseModel(title, description, datasetUri);
    String triples = modelToString(baseDatasetModel);
    HttpEntity<String> requestEntity = new HttpEntity<>(triples, httpHeaders);

    try {
      ResponseEntity<String> response = restTemplate.postForEntity(webConstants.getTriplestoreBaseUri() + PATH, requestEntity, String.class);
      if (!HttpStatus.CREATED.equals(response.getStatusCode())) {
        logger.error("error , could not create dataset, tripleStore responce = " + response.getStatusCode().getReasonPhrase());
        throw new CreateDatasetException();
      }
      URI location = response.getHeaders().getLocation();
      //store link from uri to location
      versionMappingRepository.save(new VersionMapping(location.toString(), account.getEmail(), datasetUri));
    } catch (RestClientException e) {
      logger.error(e.toString());
      throw new CreateDatasetException();
    }
    return new URI(datasetUri);
  }

  private String modelToString(Model model) {
    StringWriter outputWriter = new StringWriter();
    model.write(outputWriter, "Turtle");
    return outputWriter.toString();
  }

  private Model buildDatasetBaseModel(String title, String description, String datasetUri) {

    Model model = ModelFactory.createDefaultModel();

    Resource resource = model.createResource(datasetUri);
    resource.addProperty(DCTerms.title, title);
    if (StringUtils.isNotEmpty(description)) {
      resource.addProperty(DCTerms.description, description);
    }

    return model;
  }


}

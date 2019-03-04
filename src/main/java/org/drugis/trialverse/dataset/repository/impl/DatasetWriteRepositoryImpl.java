package org.drugis.trialverse.dataset.repository.impl;


import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.protocol.HTTP;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.WebContent;
import org.apache.jena.vocabulary.DCTerms;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.drugis.trialverse.dataset.exception.EditDatasetException;
import org.drugis.trialverse.dataset.factory.JenaFactory;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.repository.DatasetWriteRepository;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.dataset.exception.CreateDatasetException;
import org.drugis.trialverse.security.TrialversePrincipal;
import org.drugis.addis.util.WebConstants;
import org.drugis.trialverse.util.Namespaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.inject.Inject;

import java.io.IOException;
import java.io.InputStream;
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
  public static final String EDIT_TITLE_MESSAGE = "Edited title.";
  final static String EDIT_DATASET = TriplestoreService.loadResource("sparql/editDataset.sparql");
  final static String INSERT_DESCRIPTION = TriplestoreService.loadResource("sparql/insertDescription.sparql");

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
  private static final String DUMP_ENDPOINT = "/dump";
  
  @Override
  public URI createDataset(String title, String description, TrialversePrincipal owner) throws URISyntaxException, CreateDatasetException {
    String datasetUri = jenaFactory.createDatasetURI();
    Model baseDatasetModel = buildDatasetBaseModel(title, description, datasetUri);
    String triples = modelToString(baseDatasetModel);
    return createDataset(owner, datasetUri, triples);
  }

  @Override
  public URI createOrUpdateDatasetWithContent(final InputStream content, String contentType, String trialverseUri, TrialversePrincipal owner, String commitTitle, String commitDescription)
      throws URISyntaxException, CreateDatasetException {
    // find the dataset if it exists, otherwise create a new one
    URI datasetUri = new URI(trialverseUri);
    VersionMapping versionMapping = null;
    try {
    	versionMapping = versionMappingRepository.getVersionMappingByDatasetUrl(datasetUri);
    } catch (EmptyResultDataAccessException e) {
      datasetUri = createDataset(owner, trialverseUri, ""); // initialize empty
      versionMapping = versionMappingRepository.getVersionMappingByDatasetUrl(datasetUri);    	
    }

    // upload the content to the dump endpoint
    UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(versionMapping.getVersionedDatasetUrl())
        .path(DUMP_ENDPOINT)
        .build();
    HttpHeaders headers = createEventSourcingHeaders(owner, commitTitle, contentType);
    final RequestCallback requestCallback = new RequestCallback() {
      @Override
     public void doWithRequest(final ClientHttpRequest request) throws IOException {
        request.getHeaders().putAll(headers);
        IOUtils.copy(content, request.getBody());
      }
    };
    final ResponseExtractor<HttpStatus> statusExtractor = new ResponseExtractor<HttpStatus>() {
			@Override
			public HttpStatus extractData(ClientHttpResponse response) throws IOException {
				return response.getStatusCode();
			}
		};
    try {
    	HttpStatus status = restTemplate.execute(uriComponents.toUri(), HttpMethod.PUT, requestCallback, statusExtractor);
	    if (!HttpStatus.CREATED.equals(status) && !HttpStatus.OK.equals(status)) {
	    	logger.error("Got response status " + status.toString() + " expected 200 OK or 201 CREATED");
	    	throw new CreateDatasetException();
	    }
    } catch (RestClientException e) {
    	logger.error(e.toString());
    	throw new CreateDatasetException();
    }
    
    return datasetUri;
  }

  @Caching(evict = {
          @CacheEvict(cacheNames = "datasetHistory", key="#mapping.getVersionedDatasetUrl()"),
          @CacheEvict(cacheNames = "versionedDataset", key = "#mapping.trialverseDatasetUrl+'headVersion'")
  })
  public String editDataset(TrialversePrincipal owner, VersionMapping mapping, String title, String description) throws EditDatasetException {
    String editDatasetQuery = EDIT_DATASET.replace("$newTitle", title)
            .replace("$datasetUri", mapping.getTrialverseDatasetUrl());
    if(description != null) {
      editDatasetQuery = editDatasetQuery.concat(INSERT_DESCRIPTION
              .replace("$newDescription", description).replace("$datasetUri", mapping.getTrialverseDatasetUrl()));
    }
    String updateUri = mapping.getVersionedDatasetUrl() + "/update";
    HttpHeaders httpHeaders = createEventSourcingHeaders(owner, EDIT_TITLE_MESSAGE, WebContent.contentTypeSPARQLUpdate);

    HttpEntity<?> requestEntity = new HttpEntity<>(editDatasetQuery, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(updateUri, requestEntity, String.class);
    if(!HttpStatus.OK.equals(response.getStatusCode())) {
      logger.error("Error updating dataset, triplestore response = " + response.getStatusCode().getReasonPhrase());
      throw new EditDatasetException();
    }
    return response.getHeaders().get(WebConstants.X_EVENT_SOURCE_VERSION).get(0);
  }

  private URI createDataset(TrialversePrincipal owner, String datasetUri, String defaultGraphContent) throws URISyntaxException, CreateDatasetException {
    HttpHeaders httpHeaders = createEventSourcingHeaders(owner, INITIAL_COMMIT_MESSAGE, RDFLanguages.TURTLE.getContentType().getContentType());
    HttpEntity<String> requestEntity = new HttpEntity<>(defaultGraphContent, httpHeaders);
    Account account = accountRepository.findAccountByUsername(owner.getUserName());

    try {
      ResponseEntity<String> response = restTemplate.postForEntity(webConstants.getTriplestoreBaseUri() + PATH, requestEntity, String.class);
      if (!HttpStatus.CREATED.equals(response.getStatusCode())) {
        logger.error("error , could not create dataset, tripleStore response = " + response.getStatusCode().getReasonPhrase());
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

  private HttpHeaders createEventSourcingHeaders(TrialversePrincipal owner, String commitTitle, String contentType) {
    HttpHeaders httpHeaders = new HttpHeaders();
    Account account = accountRepository.findAccountByUsername(owner.getUserName());
    if(owner.hasApiKey()) {
      httpHeaders.add(WebConstants.EVENT_SOURCE_CREATOR_HEADER,
              "https://trialverse.org/apikeys/" + owner.getApiKey().getId());
    } else {
      httpHeaders.add(WebConstants.EVENT_SOURCE_CREATOR_HEADER, "mailto:" + account.getEmail());
    }
    httpHeaders.add(WebConstants.EVENT_SOURCE_TITLE_HEADER, Base64.encodeBase64String(commitTitle.getBytes()));
    httpHeaders.add(HTTP.CONTENT_TYPE, contentType);
    return httpHeaders;
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

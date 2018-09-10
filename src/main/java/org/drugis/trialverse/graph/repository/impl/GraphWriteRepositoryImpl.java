package org.drugis.trialverse.graph.repository.impl;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.util.EntityUtils;
import org.apache.jena.riot.RDFLanguages;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.AuthenticationService;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.util.WebConstants;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.graph.exception.DeleteGraphException;
import org.drugis.trialverse.graph.exception.UpdateGraphException;
import org.drugis.trialverse.graph.repository.GraphWriteRepository;
import org.drugis.trialverse.graph.service.GraphService;
import org.drugis.trialverse.security.TrialversePrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Repository;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * Created by daan on 20-11-14.
 */
@Repository
public class GraphWriteRepositoryImpl implements GraphWriteRepository {

  public static final String GRAPH_QUERY_STRING = "?graph={graphUri}";

  @Inject
  private VersionMappingRepository versionMappingRepository;

  @Inject
  private HttpClient httpClient;

  @Inject
  private AuthenticationService authenticationService;

  @Inject
  private AccountRepository accountRepository;

  @Inject
  private GraphService graphService;

  private static final String DATA_ENDPOINT = "/data";
  private final static Logger logger = LoggerFactory.getLogger(GraphWriteRepositoryImpl.class);

  @Override
  @Caching(evict = {
          @CacheEvict(cacheNames = "datasetHistory", key = "#datasetUri.toString()"),
          @CacheEvict(cacheNames = "versionedDatasetQuery", allEntries = true)})
  public Header updateGraph(URI datasetUri, String graphUuid, InputStream graph, String commitTitle, String commitDescription) throws IOException, UpdateGraphException {

    UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(datasetUri.toString())
            .path(DATA_ENDPOINT)
            .queryParam("graph", graphService.buildGraphUri(graphUuid))
            .build();

    HttpRequestBase putRequest = new HttpPut(uriComponents.toUri());
    putRequest.setHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, RDFLanguages.TURTLE.getContentType().getContentType());
    putRequest.setHeader(WebConstants.EVENT_SOURCE_TITLE_HEADER, Base64.encodeBase64String(commitTitle.getBytes()));

    putRequest = addCreatorToRequest(putRequest);

    if (StringUtils.isNotEmpty(commitDescription)) {
      putRequest.setHeader(WebConstants.EVENT_SOURCE_DESCRIPTION_HEADER, Base64.encodeBase64String(commitDescription.getBytes()));
    }

    HttpEntity putBody = new InputStreamEntity(graph);

    ((HttpPut) putRequest).setEntity(putBody);
    logger.debug("execute updateGraph");

    Header versionHeader;
    try (CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(putRequest)) {
      versionHeader = response.getFirstHeader(WebConstants.X_EVENT_SOURCE_VERSION);
      EntityUtils.consume(response.getEntity());
      return versionHeader;
    } catch (Exception e) {
      logger.debug("error updating graph {}", e);
      throw new UpdateGraphException();
    }
  }

  @Override
  @Caching(evict = {
          @CacheEvict(cacheNames = "datasetHistory", key = "#datasetUri.toString()"),
          @CacheEvict(cacheNames = "versionedDatasetQuery", allEntries = true)})
  public Header deleteGraph(URI datasetUri, String graphUuid) throws DeleteGraphException {

    UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(datasetUri.toString())
            .path(DATA_ENDPOINT)
            .queryParam("graph", graphService.buildGraphUri(graphUuid))
            .build();

    HttpRequestBase deleteRequest = new HttpDelete(uriComponents.toUri());

    deleteRequest = addCreatorToRequest(deleteRequest);
    deleteRequest.setHeader(WebConstants.EVENT_SOURCE_TITLE_HEADER, Base64.encodeBase64String("Deleted graph.".getBytes()));

    Header versionHeader;
    try (CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(deleteRequest)) {
      versionHeader = response.getFirstHeader(WebConstants.X_EVENT_SOURCE_VERSION);
      EntityUtils.consume(response.getEntity());
      return versionHeader;
    } catch (Exception e) {
      logger.debug("error deleting graph {}", e);
      throw new DeleteGraphException();
    }

  }

  private HttpRequestBase addCreatorToRequest(HttpRequestBase deleteRequest) {
    TrialversePrincipal owner = authenticationService.getAuthentication();
    Account user = accountRepository.findAccountByUsername(owner.getUserName());

    if (owner.hasApiKey()) {
      deleteRequest.setHeader(WebConstants.EVENT_SOURCE_CREATOR_HEADER, "https://trialverse.org/apikeys/" + owner.getApiKey().getId());
    } else {
      deleteRequest.setHeader(WebConstants.EVENT_SOURCE_CREATOR_HEADER, "mailto:" + user.getEmail());
    }
    return deleteRequest;
  }
}

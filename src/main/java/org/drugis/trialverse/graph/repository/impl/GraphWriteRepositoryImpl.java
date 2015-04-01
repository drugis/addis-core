package org.drugis.trialverse.graph.repository.impl;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.util.EntityUtils;
import org.apache.jena.riot.RDFLanguages;
import org.drugis.trialverse.dataset.factory.HttpClientFactory;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.exception.UpdateGraphException;
import org.drugis.trialverse.graph.repository.GraphWriteRepository;
import org.drugis.trialverse.security.AuthenticationService;
import org.drugis.trialverse.util.Namespaces;
import org.drugis.trialverse.util.WebConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.http.HttpEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
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

  public static final String DATA_ENDPOINT = "/data";

  private final static Logger logger = LoggerFactory.getLogger(GraphWriteRepositoryImpl.class);

  @Override
  public Header updateGraph(URI datasetUri, String graphUuid, HttpServletRequest request) throws IOException, UpdateGraphException {
    VersionMapping versionMapping = versionMappingRepository.getVersionMappingByDatasetUrl(datasetUri);

    UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(versionMapping.getVersionedDatasetUrl())
            .path(DATA_ENDPOINT)
            .queryParam("graph", Namespaces.GRAPH_NAMESPACE + graphUuid)
            .build();

    HttpPut putRequest = new HttpPut(uriComponents.toUri());

    putRequest.setHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, RDFLanguages.TURTLE.getContentType().getContentType());
    String title = request.getParameter(WebConstants.COMMIT_TITLE_PARAM);
    putRequest.setHeader(WebConstants.EVENT_SOURCE_TITLE_HEADER, Base64.encodeBase64String(title.getBytes()));
    putRequest.setHeader(WebConstants.EVENT_SOURCE_CREATOR_HEADER, "mailto:" + authenticationService.getAuthentication().getName());

    String commitDescription = request.getParameter(WebConstants.COMMIT_DESCRIPTION_PARAM);
    if(StringUtils.isNotEmpty(commitDescription)) {
      putRequest.setHeader(WebConstants.EVENT_SOURCE_DESCRIPTION_HEADER, Base64.encodeBase64String(commitDescription.getBytes()));
    }

    HttpEntity putBody = new InputStreamEntity(request.getInputStream());

    putRequest.setEntity(putBody);
    logger.debug("execute updateGraph");

    Header versionHeader;
    try(CloseableHttpResponse response =  (CloseableHttpResponse) httpClient.execute(putRequest)) {
      versionHeader = response.getFirstHeader(WebConstants.X_EVENT_SOURCE_VERSION);
      EntityUtils.consume(response.getEntity());
      return versionHeader;
    } catch(Exception e) {
      logger.debug("error updating graph {}", e);
      throw new UpdateGraphException();
    }
  }
}

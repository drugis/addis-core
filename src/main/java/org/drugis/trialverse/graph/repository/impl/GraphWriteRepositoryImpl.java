package org.drugis.trialverse.graph.repository.impl;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.jena.riot.RDFLanguages;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.graph.repository.GraphWriteRepository;
import org.drugis.trialverse.security.Account;
import org.drugis.trialverse.security.AuthenticationService;
import org.drugis.trialverse.util.InputStreamMessageConverter;
import org.drugis.trialverse.util.Namespaces;
import org.drugis.trialverse.util.WebConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.Principal;

/**
 * Created by daan on 20-11-14.
 */
@Repository
public class GraphWriteRepositoryImpl implements GraphWriteRepository {

  public static final String GRAPH_QUERY_STRING = "?graph={graphUri}";

  @Inject
  private VersionMappingRepository versionMappingRepository;

  @Inject
  private RestTemplate restTemplate;

  @Inject
  private AuthenticationService authenticationService;

  public static final String DATA_ENDPOINT = "/data";

  private final static Logger logger = LoggerFactory.getLogger(GraphWriteRepositoryImpl.class);

  @Override
  public void updateGraph(URI datasetUri, String graphUuid, HttpServletRequest request) throws IOException {
    VersionMapping versionMapping = versionMappingRepository.getVersionMappingByDatasetUrl(datasetUri);
    String uri = versionMapping.getVersionedDatasetUrl() + DATA_ENDPOINT + GRAPH_QUERY_STRING;
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(org.apache.http.HttpHeaders.CONTENT_TYPE, RDFLanguages.TURTLE.getContentType().getContentType());
    String title = request.getParameter(WebConstants.COMMIT_TITLE_PARAM);
    httpHeaders.add(WebConstants.EVENT_SOURCE_TITLE_HEADER, Base64.encodeBase64String(title.getBytes()));
    httpHeaders.add(WebConstants.EVENT_SOURCE_CREATOR_HEADER, "mailto:" + authenticationService.getAuthentication().getName());
    String commitDescription = request.getParameter(WebConstants.COMMIT_DESCRIPTION_PARAM);
    if(StringUtils.isNotEmpty(commitDescription)) {
      httpHeaders.add(WebConstants.EVENT_SOURCE_DESCRIPTION_HEADER, Base64.encodeBase64String(commitDescription.getBytes()));
    }
    HttpEntity<InputStream> requestEntity = new HttpEntity<InputStream>(request.getInputStream(), httpHeaders);
    restTemplate.getMessageConverters().add(new InputStreamMessageConverter());
    restTemplate.put(uri, requestEntity, Namespaces.GRAPH_NAMESPACE + graphUuid);
  }
}

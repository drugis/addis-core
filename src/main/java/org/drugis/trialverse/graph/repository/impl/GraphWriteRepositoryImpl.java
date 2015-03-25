package org.drugis.trialverse.graph.repository.impl;

import org.apache.jena.riot.RDFLanguages;
import org.drugis.trialverse.dataset.factory.HttpClientFactory;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.graph.repository.GraphWriteRepository;
import org.drugis.trialverse.util.InputStreamMessageConverter;
import org.drugis.trialverse.util.Namespaces;
import org.drugis.trialverse.util.WebConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
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
  private RestTemplate restTemplate;

  public static final String DATA_ENDPOINT = "/data";

  private final static Logger logger = LoggerFactory.getLogger(GraphWriteRepositoryImpl.class);

  @Override
  public void updateGraph(URI datasetUri, String graphUuid, InputStream content) {
    VersionMapping versionMapping = versionMappingRepository.getVersionMappingByDatasetUrl(datasetUri);
    String uri = versionMapping.getVersionedDatasetUrl() + DATA_ENDPOINT + GRAPH_QUERY_STRING;
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(org.apache.http.HttpHeaders.CONTENT_TYPE, RDFLanguages.TURTLE.getContentType().getContentType());
    HttpEntity<InputStream> requestEntity = new HttpEntity<>(content, httpHeaders);
    restTemplate.getMessageConverters().add(new InputStreamMessageConverter());
    restTemplate.put(uri, requestEntity, Namespaces.GRAPH_NAMESPACE + graphUuid);
  }
}

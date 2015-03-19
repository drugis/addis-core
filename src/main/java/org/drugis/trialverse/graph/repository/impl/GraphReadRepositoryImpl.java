package org.drugis.trialverse.graph.repository.impl;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.jena.riot.RDFLanguages;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.graph.repository.GraphReadRepository;
import org.drugis.trialverse.util.Namespaces;
import org.springframework.stereotype.Repository;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;

/**
 * Created by connor on 28-11-14.
 */
@Repository
public class GraphReadRepositoryImpl implements GraphReadRepository {

  public static final String DATA_ENDPOINT = "/data";
  public static final String GRAPH = "graph";
  @Inject
  private HttpClient httpClient;

  @Inject
  private VersionMappingRepository versionMappingRepository;

  @Override
  public HttpResponse getStudy(URI trialverseDatasetUri, String studyUUID) throws IOException {
    VersionMapping versionMapping = versionMappingRepository.getVersionMappingByDatasetUrl(trialverseDatasetUri);

    UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(versionMapping.getVersionedDatasetUrl())
            .path(DATA_ENDPOINT)
            .queryParam(GRAPH, Namespaces.STUDY_NAMESPACE + studyUUID)
            .build();
    HttpGet request = new HttpGet(uriComponents.toUri());
    request.addHeader(org.apache.http.HttpHeaders.ACCEPT, RDFLanguages.TURTLE.getContentType().getContentType());
    return httpClient.execute(request);
  }

}

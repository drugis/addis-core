package org.drugis.trialverse.graph.repository;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.IOException;
import java.net.URI;

/**
 * Created by connor on 28-11-14.
 */
public interface GraphReadRepository {

  public CloseableHttpResponse getGraph(URI trialverseDatasetUri, String graphUuid) throws IOException;
}

package org.drugis.trialverse.graph.repository;

import org.apache.http.HttpResponse;

import java.io.IOException;
import java.net.URI;

/**
 * Created by connor on 28-11-14.
 */
public interface GraphReadRepository {

  public HttpResponse getStudy(URI trialverseDatasetUri, String studyUUID) throws IOException;
}

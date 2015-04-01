package org.drugis.trialverse.graph.repository;

import org.apache.http.HttpResponse;
import org.drugis.trialverse.exception.ReadGraphException;


import java.io.IOException;
import java.net.URI;

/**
 * Created by connor on 28-11-14.
 */
public interface GraphReadRepository {

  public byte[] getGraph(URI trialverseDatasetUri, String graphUUID) throws IOException, ReadGraphException;
}

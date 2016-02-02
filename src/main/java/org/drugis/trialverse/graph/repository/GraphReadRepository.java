package org.drugis.trialverse.graph.repository;

import org.drugis.trialverse.graph.exception.ReadGraphException;


import java.io.IOException;
import java.net.URI;

/**
 * Created by connor on 28-11-14.
 */
public interface GraphReadRepository {

  byte[] getGraph(String versionedDatasetUrl, String versionUuid, String graphUUID, String contentType) throws IOException, ReadGraphException;
}

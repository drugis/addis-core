package org.drugis.trialverse.graph.repository;

import java.io.InputStream;
import java.net.URI;

/**
 * Created by daan on 20-11-14.
 */
public interface GraphWriteRepository {


  public void updateGraph(URI datasetUri, String graphUuid, InputStream content);
}

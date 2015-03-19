package org.drugis.trialverse.graph.repository;

import org.apache.http.HttpResponse;

import java.io.InputStream;
import java.net.URI;

/**
 * Created by daan on 20-11-14.
 */
public interface GraphWriteRepository {


  public void updateStudy(URI datasetUri, String studyUUID, InputStream content);
}

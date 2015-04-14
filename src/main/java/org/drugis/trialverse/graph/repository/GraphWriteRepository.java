package org.drugis.trialverse.graph.repository;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.drugis.trialverse.exception.UpdateGraphException;
import org.drugis.trialverse.security.Account;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * Created by daan on 20-11-14.
 */
public interface GraphWriteRepository {

  public Header updateGraph(URI datasetUri, String graphUuid, HttpServletRequest request) throws IOException, UpdateGraphException;
}

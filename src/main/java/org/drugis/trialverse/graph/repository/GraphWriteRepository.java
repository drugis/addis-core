package org.drugis.trialverse.graph.repository;

import org.apache.http.Header;
import org.drugis.trialverse.graph.exception.UpdateGraphException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * Created by daan on 20-11-14.
 */
public interface GraphWriteRepository {

  Header updateGraph(URI datasetUri, String graphUuid, InputStream graph, String commitTitle, String commitDescription) throws IOException, UpdateGraphException;
}

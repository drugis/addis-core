package org.drugis.trialverse.graph.service;

import org.drugis.trialverse.dataset.exception.RevisionNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by daan on 26-8-15.
 */
public interface GraphService {
  URI extractGraphUri(URI sourceGraphUri);

  URI copy(URI targetDatasetUri, URI targetGraphUri, URI copyOfUri) throws URISyntaxException, IOException, RevisionNotFoundException;

  URI extractDatasetUri(URI sourceGraphUri);

  URI extractVersionUri(URI sourceGraphUri);

  InputStream jsonGraphInputStreamToTurtleInputStream(InputStream jsonGraph);
}

package org.drugis.trialverse.graph.service;

import org.drugis.trialverse.dataset.exception.RevisionNotFoundException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by daan on 26-8-15.
 */
public interface GraphService {
  URI copy(URI targetDatasetUri, URI targetGraphUri, URI sourceDatasetUri, URI sourceVersionUri, URI sourceGraphUri) throws URISyntaxException, IOException, RevisionNotFoundException;

  String extractDatasetUuid(String sourceGraphUri);

  String extractVersionUuid(String sourceGraphUri);

  String extractGraphUuid(String sourceGraphUri);
}

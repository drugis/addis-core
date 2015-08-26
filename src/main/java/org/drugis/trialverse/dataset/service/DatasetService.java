package org.drugis.trialverse.dataset.service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by daan on 26-8-15.
 */
public interface DatasetService {
  URI copy(URI targetDatasetUri, URI targetGraphUri, URI sourceDatasetUri, URI sourceGraphUri, URI sourceVersionUri) throws URISyntaxException, IOException;
}

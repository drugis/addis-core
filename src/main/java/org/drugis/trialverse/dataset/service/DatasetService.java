package org.drugis.trialverse.dataset.service;

import java.net.URI;

/**
 * Created by daan on 26-8-15.
 */
public interface DatasetService {
  URI copy(URI targetDatasetUri, URI targetGraphUri, URI sourceGraphUri, URI sourceVersionUri);
}

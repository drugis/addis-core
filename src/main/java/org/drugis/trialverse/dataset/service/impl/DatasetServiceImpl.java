package org.drugis.trialverse.dataset.service.impl;

import org.drugis.trialverse.dataset.service.DatasetService;
import org.springframework.stereotype.Service;

import java.net.URI;

/**
 * Created by daan on 26-8-15.
 */
@Service
public class DatasetServiceImpl implements DatasetService{
  @Override
  public URI copy(URI targetDatasetUri, URI targetGraphUri, URI sourceGraphUri, URI sourceVersionUri) {
    return null;
  }
}

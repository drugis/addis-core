package org.drugis.trialverse.dataset.service.impl;

import org.apache.jena.rdf.model.Model;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.repository.DatasetReadRepository;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.dataset.service.DatasetService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by daan on 26-8-15.
 */
@Service
public class DatasetServiceImpl implements DatasetService{

  @Inject
  private DatasetReadRepository datasetReadRepository;

  @Inject
  private VersionMappingRepository versionMappingRepository;

  @Override
  public URI copy(URI targetDatasetUri, URI targetGraphUri, URI sourceDatasetUri, URI sourceGraphUri, URI sourceVersionUri) throws URISyntaxException, IOException {
    VersionMapping sourceDatasetEventSourceUri = versionMappingRepository.getVersionMappingByDatasetUrl(sourceDatasetUri);
    Model history = datasetReadRepository.getHistory(sourceDatasetEventSourceUri.getVersionedDatasetUri());
    return null;
  }
}

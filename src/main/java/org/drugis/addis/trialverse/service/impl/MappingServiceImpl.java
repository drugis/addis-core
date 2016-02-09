package org.drugis.addis.trialverse.service.impl;

import org.drugis.addis.trialverse.service.MappingService;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.util.Namespaces;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by daan on 9-2-16.
 */
@Service
public class MappingServiceImpl implements MappingService {

  @Inject
  VersionMappingRepository versionMappingRepository;

  @Override
  public String getVersionedUuid(String namespaceUid) throws URISyntaxException {
    VersionMapping mapping = versionMappingRepository.getVersionMappingByDatasetUrl(new URI(Namespaces.DATASET_NAMESPACE + namespaceUid));
    URI versionedDatasetUri = mapping.getVersionedDatasetUri();
    return versionedDatasetUri.toString().split("/datasets/")[1];

  }
}

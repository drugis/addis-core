package org.drugis.trialverse.dataset.repository;

import org.drugis.trialverse.dataset.model.VersionMapping;

import java.net.URI;
import java.util.List;

/**
 * Created by connor on 10-3-15.
 */
public interface VersionMappingRepository {

  void save(VersionMapping versionMapping);

  List<VersionMapping> findMappingsByEmail(String username);

  List<VersionMapping> findMappingsByTrialverseDatasetUrls(List<String> datasetUrls);

  List<VersionMapping> getVersionMappings();

  VersionMapping getVersionMappingByDatasetUrl(URI trialverseDatasetUrl);

  VersionMapping getVersionMappingByVersionedURl(URI sourceDatasetUri);

  void setArchivedStatus(URI datasetUri, Boolean archived);
}

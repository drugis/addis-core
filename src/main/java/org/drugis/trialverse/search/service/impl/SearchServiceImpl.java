package org.drugis.trialverse.search.service.impl;

import org.apache.jena.atlas.json.JsonObject;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.repository.DatasetReadRepository;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.search.controller.StudySearchResult;
import org.drugis.trialverse.search.service.SearchService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Created by connor on 01/09/15.
 */
@Service
public class SearchServiceImpl implements SearchService {

  @Inject
  VersionMappingRepository versionMappingRepository;

  @Inject
  DatasetReadRepository datasetReadRepository;

  @Override
  public List<StudySearchResult> searchStudy(String searchTerm) throws IOException, URISyntaxException {
    List<VersionMapping> mappings = versionMappingRepository.getVersionMappings();
    for (VersionMapping mapping : mappings) {
      mapping.getVersionedDatasetUrl();
      JsonObject queryResult = datasetReadRepository.executeHeadQuery(searchTerm, mapping);
      // create result object
    }
    return null;
  }
}

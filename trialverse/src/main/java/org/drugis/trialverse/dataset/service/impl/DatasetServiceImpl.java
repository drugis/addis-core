package org.drugis.trialverse.dataset.service.impl;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.drugis.trialverse.dataset.model.Dataset;
import org.drugis.trialverse.dataset.repository.DatasetReadRepository;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.dataset.service.DatasetService;
import org.drugis.trialverse.security.Account;
import org.drugis.trialverse.util.JenaProperties;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by connor on 9/8/15.
 */
@Service
public class DatasetServiceImpl implements DatasetService {

  @Inject
  private VersionMappingRepository versionMappingRepository;

  @Inject
  private DatasetReadRepository datasetReadRepository;

  @Override
  public List<Dataset> findDatasets(Account user) {
    return versionMappingRepository.findMappingsByUsername(user.getUsername())
            .stream()
            .map((mapping) -> {
              String trialverseDatasetUrl = mapping.getTrialverseDatasetUrl();
              Model dataset = datasetReadRepository.queryDataset(mapping);
              Statement titleTriple = dataset.getProperty(dataset.getResource(trialverseDatasetUrl), JenaProperties.TITLE_PROPERTY);
              Statement descriptionTriple = dataset.getProperty(dataset.getResource(trialverseDatasetUrl), JenaProperties.DESCRIPTION_PROPERTY);
              String description = descriptionTriple != null ? descriptionTriple.getObject().toString() : null;
              String title = titleTriple.getObject().toString();
              return new Dataset(trialverseDatasetUrl, user, title, description);
            })
            .collect(Collectors.toList());
  }

}

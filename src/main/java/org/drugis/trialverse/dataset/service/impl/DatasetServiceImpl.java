package org.drugis.trialverse.dataset.service.impl;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.trialverse.dataset.model.Dataset;
import org.drugis.trialverse.dataset.model.FeaturedDataset;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.repository.DatasetReadRepository;
import org.drugis.trialverse.dataset.repository.FeaturedDatasetRepository;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.dataset.service.DatasetService;
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

  @Inject
  private FeaturedDatasetRepository featuredDatasetRepository;

  @Inject
  private AccountRepository accountRepository;

  @Override
  public List<Dataset> findDatasets(Account user) {
    return versionMappingRepository.findMappingsByEmail(user.getEmail())
            .stream()
            .map((mapping) -> {
              String trialverseDatasetUrl = mapping.getTrialverseDatasetUrl();
              Model dataset = datasetReadRepository.queryDataset(mapping);
              return buildDataset(dataset, user, trialverseDatasetUrl);
            })
            .collect(Collectors.toList());
  }

  @Override
  public List<Dataset> findFeatured() {
    List<FeaturedDataset> featuredDatasets = featuredDatasetRepository.findAll();
    List<VersionMapping> versionMappings = versionMappingRepository.findMappingsByTrialverseDatasetUrls(featuredDatasets.stream().map(FeaturedDataset::getDatasetUrl).collect(Collectors.toList()));
    List<Dataset> datasets = versionMappings.stream()
            .map((mapping) -> {
              Account user = accountRepository.findAccountByEmail(mapping.getOwnerUuid());
              String trialverseDatasetUrl = mapping.getTrialverseDatasetUrl();
              Model dataset = datasetReadRepository.queryDataset(mapping);
              return buildDataset(dataset, user, trialverseDatasetUrl);
            })
            .collect(Collectors.toList());
    return datasets;
  }

  private Dataset buildDataset(Model model, Account user, String trialverseDatasetUrl) {
    Statement titleTriple = model.getProperty(model.getResource(trialverseDatasetUrl), JenaProperties.TITLE_PROPERTY);
    Statement descriptionTriple = model.getProperty(model.getResource(trialverseDatasetUrl), JenaProperties.DESCRIPTION_PROPERTY);
    Statement headVersionTriple = model.getProperty(model.getResource(trialverseDatasetUrl), JenaProperties.headVersionProperty);
    String description = descriptionTriple != null ? descriptionTriple.getObject().toString() : null;
    String title = titleTriple != null ? titleTriple.getObject().toString() : null;
    String headVersion = headVersionTriple.getObject().toString();
    return new Dataset(trialverseDatasetUrl, user, title, description, headVersion);
  }
}

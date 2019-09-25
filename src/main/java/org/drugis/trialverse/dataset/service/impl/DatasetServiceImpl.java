package org.drugis.trialverse.dataset.service.impl;

import org.apache.jena.rdf.model.*;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.trialverse.dataset.model.Dataset;
import org.drugis.trialverse.dataset.model.FeaturedDataset;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.repository.DatasetReadRepository;
import org.drugis.trialverse.dataset.repository.FeaturedDatasetRepository;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.dataset.service.DatasetService;
import org.drugis.trialverse.security.TrialversePrincipal;
import org.drugis.trialverse.util.JenaProperties;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.security.Principal;
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
              Model dataset = datasetReadRepository.queryDataset(mapping);
              return buildDataset(dataset, user, mapping);
            })
            .collect(Collectors.toList());
  }

  @Override
  public List<Dataset> findFeatured() {
    List<FeaturedDataset> featuredDatasets = featuredDatasetRepository.findAll();
    List<VersionMapping> versionMappings = versionMappingRepository.findMappingsByTrialverseDatasetUrls(featuredDatasets.stream().map(FeaturedDataset::getDatasetUrl).collect(Collectors.toList()));
    return versionMappings.stream()
            .map((mapping) -> {
              Account user = accountRepository.findAccountByEmail(mapping.getOwnerUuid());
              Model dataset = datasetReadRepository.queryDataset(mapping);
              return buildDataset(dataset, user, mapping);
            })
            .collect(Collectors.toList());
  }

  @Override
  public void checkDatasetOwner(Integer datasetOwnerId, Principal currentUser) throws MethodNotAllowedException {
    TrialversePrincipal principal = new TrialversePrincipal(currentUser);
    Account user = accountRepository.findAccountByUsername(principal.getUserName());
    if (user == null || !datasetOwnerId.equals(user.getId())) {
      throw new MethodNotAllowedException();
    }
  }

  private Dataset buildDataset(Model model, Account user, VersionMapping mapping) {
    String datasetUrl = mapping.getTrialverseDatasetUrl();
    Boolean archived = mapping.getArchived();
    String archivedOn = mapping.getArchivedOn();
    NodeIterator titleIterator = model.listObjectsOfProperty(JenaProperties.TITLE_PROPERTY);
    String title = titleIterator.next().toString();

    NodeIterator headVersions = model.listObjectsOfProperty(JenaProperties.headVersionProperty);
    String headVersion = headVersions.next().toString();

    NodeIterator descriptionIterator = model.listObjectsOfProperty(JenaProperties.DESCRIPTION_PROPERTY);
    String description = descriptionIterator.hasNext() ? descriptionIterator.next().toString() : null;

    return new Dataset(datasetUrl, user, title, description, headVersion, archived, archivedOn);
  }
}

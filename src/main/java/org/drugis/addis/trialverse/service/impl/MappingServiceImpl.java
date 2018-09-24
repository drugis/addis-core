package org.drugis.addis.trialverse.service.impl;

import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.trialverse.model.mapping.TriplestoreUuidAndOwner;
import org.drugis.addis.trialverse.service.MappingService;
import org.drugis.addis.util.WebConstants;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.util.Namespaces;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.net.URI;

/**
 * Created by daan on 9-2-16.
 */
@Service
public class MappingServiceImpl implements MappingService {

  @Inject
  private VersionMappingRepository versionMappingRepository;

  @Inject
  private WebConstants webConstants;

  @Inject
  private AccountRepository accountRepository;

  @Override
  public String getVersionedUuid(String namespaceUid) {
    VersionMapping mapping = versionMappingRepository.getVersionMappingByDatasetUrl(URI.create(Namespaces.DATASET_NAMESPACE + namespaceUid));
    return mapping.getVersionedDatasetUri().toString().split("/datasets/")[1];
  }

  @Override
  public TriplestoreUuidAndOwner getVersionedUuidAndOwner(String namespaceUuid) {
    VersionMapping mapping = versionMappingRepository.getVersionMappingByDatasetUrl(URI.create(Namespaces.DATASET_NAMESPACE + namespaceUuid));
    String versionedUuid = mapping.getVersionedDatasetUri().toString().split("/datasets/")[1];
    Account account = accountRepository.findAccountByEmail(mapping.getOwnerUuid());
    return new TriplestoreUuidAndOwner(versionedUuid, account.getId());
  }

  @Override
  public String getJenaURL(VersionMapping mapping) {
    String versionedDatasetUrl = mapping.getVersionedDatasetUrl();
    String datasetUuid = versionedDatasetUrl.split("/datasets/")[1];
    return webConstants.getTriplestoreBaseUri() + "/datasets/" + datasetUuid;
  }
}

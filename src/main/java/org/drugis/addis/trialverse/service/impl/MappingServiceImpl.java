package org.drugis.addis.trialverse.service.impl;

import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.trialverse.model.mapping.VersionedUuidAndOwner;
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

  @Inject
  AccountRepository accountRepository;

  @Override
  public String getVersionedUuid(String namespaceUid) throws URISyntaxException {
    VersionMapping mapping = versionMappingRepository.getVersionMappingByDatasetUrl(new URI(Namespaces.DATASET_NAMESPACE + namespaceUid));
    return mapping.getVersionedDatasetUri().toString().split("/datasets/")[1];
  }

  @Override
  public VersionedUuidAndOwner getVersionedUuidAndOwner(String namespaceUuid) throws URISyntaxException {
    VersionMapping mapping = versionMappingRepository.getVersionMappingByDatasetUrl(new URI(Namespaces.DATASET_NAMESPACE + namespaceUuid));
    String versionedUuid = mapping.getVersionedDatasetUri().toString().split("/datasets/")[1];
    Account account = accountRepository.findAccountByEmail(mapping.getOwnerUuid());
    return new VersionedUuidAndOwner(versionedUuid, account.getId());
  }
}

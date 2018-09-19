package org.drugis.addis.trialverse.service;

import org.drugis.addis.trialverse.model.mapping.TriplestoreUuidAndOwner;
import org.drugis.trialverse.dataset.model.VersionMapping;

/**
 * Created by daan on 9-2-16.
 */
public interface MappingService {
  String getVersionedUuid(String namespaceUid);

  TriplestoreUuidAndOwner getVersionedUuidAndOwner(String namespaceUuid);

  String getJenaURL(VersionMapping mapping);
}

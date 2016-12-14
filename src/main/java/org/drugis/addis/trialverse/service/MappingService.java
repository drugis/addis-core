package org.drugis.addis.trialverse.service;

import org.drugis.addis.trialverse.model.mapping.TriplestoreUuidAndOwner;

import java.net.URISyntaxException;

/**
 * Created by daan on 9-2-16.
 */
public interface MappingService {
  String getVersionedUuid(String namespaceUid) throws URISyntaxException;

  TriplestoreUuidAndOwner getVersionedUuidAndOwner(String namespaceUuid) throws URISyntaxException;


}

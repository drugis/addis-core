package org.drugis.addis.trialverse.service;

import org.springframework.stereotype.Service;

import java.net.URISyntaxException;

/**
 * Created by daan on 9-2-16.
 */
public interface MappingService {
  public String getVersionedUuid(String namespaceUid) throws URISyntaxException;
}

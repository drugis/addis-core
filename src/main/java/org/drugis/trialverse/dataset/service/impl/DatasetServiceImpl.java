package org.drugis.trialverse.dataset.service.impl;

import org.drugis.trialverse.dataset.repository.DatasetReadRepository;
import org.drugis.trialverse.dataset.service.DatasetService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.security.Principal;

/**
 * Created by connor on 26-11-14.
 */
@Service
public class DatasetServiceImpl implements DatasetService {

  @Inject
  DatasetReadRepository datasetReadRepository;

  @Override
  public boolean isOwner(Principal currentUser) {
    return datasetReadRepository.isOwner(currentUser);
  }
}

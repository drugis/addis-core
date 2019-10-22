package org.drugis.trialverse.dataset.service;

import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.security.Account;
import org.drugis.trialverse.dataset.model.Dataset;

import java.security.Principal;
import java.util.List;

public interface DatasetService {
  List<Dataset> findDatasets(Account user);

  List<Dataset> findFeatured();

  void checkDatasetOwner(Integer datasetOwnerId, Principal currentUser) throws MethodNotAllowedException;
}

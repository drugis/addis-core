package org.drugis.trialverse.dataset.service;

import org.drugis.trialverse.dataset.model.Dataset;
import org.drugis.addis.security.Account;

import java.util.List;

/**
 * Created by connor on 9/8/15.
 */
public interface DatasetService {
  List<Dataset> findDatasets(Account user);
}

package org.drugis.trialverse.dataset.repository;

import org.apache.http.HttpResponse;
import org.drugis.trialverse.security.Account;

import java.io.InputStream;

/**
 * Created by connor on 04/11/14.
 */
public interface DatasetWriteRepository {
  String createDataset(String title, String description, Account owner);

  HttpResponse updateDataset(String datasetUUID, InputStream datasetContent);
}

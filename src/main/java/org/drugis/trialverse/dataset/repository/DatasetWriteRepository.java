package org.drugis.trialverse.dataset.repository;

import org.apache.http.HttpException;
import org.drugis.trialverse.dataset.exception.CreateDatasetException;
import org.drugis.trialverse.security.Account;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by connor on 04/11/14.
 */
public interface DatasetWriteRepository {
  URI createDataset(String title, String description, Account owner) throws URISyntaxException, HttpException, CreateDatasetException;
}

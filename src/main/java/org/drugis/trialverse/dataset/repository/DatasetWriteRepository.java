package org.drugis.trialverse.dataset.repository;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.drugis.trialverse.exception.CreateDatasetException;
import org.drugis.trialverse.security.Account;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by connor on 04/11/14.
 */
public interface DatasetWriteRepository {
  URI createDataset(String title, String description, Account owner) throws URISyntaxException, HttpException, CreateDatasetException;
}

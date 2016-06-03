package org.drugis.trialverse.dataset.repository;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpException;
import org.drugis.trialverse.dataset.exception.CreateDatasetException;
import org.drugis.trialverse.security.TrialversePrincipal;

/**
 * Created by connor on 04/11/14.
 */
public interface DatasetWriteRepository {
  URI createDataset(String title, String description, TrialversePrincipal owner) throws URISyntaxException, CreateDatasetException;
  URI createOrUpdateDatasetWithContent(InputStream content, String contentType, String trialverseUri, TrialversePrincipal owner, String commitTitle, String commitDescription) throws URISyntaxException, CreateDatasetException;
}

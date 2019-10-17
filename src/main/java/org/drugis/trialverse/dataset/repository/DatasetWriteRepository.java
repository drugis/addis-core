package org.drugis.trialverse.dataset.repository;

import org.drugis.trialverse.dataset.exception.CreateDatasetException;
import org.drugis.trialverse.dataset.exception.EditDatasetException;
import org.drugis.trialverse.dataset.exception.SetArchivedStatusOfDatasetException;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.security.TrialversePrincipal;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by connor on 04/11/14.
 */
public interface DatasetWriteRepository {
  URI createDataset(String title, String description, TrialversePrincipal owner) throws URISyntaxException, CreateDatasetException;
  URI createOrUpdateDatasetWithContent(InputStream content, String contentType, String trialverseUri, TrialversePrincipal owner, String commitTitle, String commitDescription) throws URISyntaxException, CreateDatasetException;
  String editDataset(TrialversePrincipal owner, VersionMapping mapping, String title, String description) throws URISyntaxException, EditDatasetException;
}

package org.drugis.trialverse.dataset.repository;

import com.hp.hpl.jena.vocabulary.RDF;
import org.apache.http.HttpResponse;
import org.drugis.trialverse.security.Account;

/**
 * Created by daan on 7-11-14.
 */
public interface DatasetReadRepository {
  public HttpResponse queryDatasets(Account currentUserAccount);

  public HttpResponse getDataset(String datasetUUID);

  public HttpResponse queryDatasetsWithDetail(String datasetUUID);
}

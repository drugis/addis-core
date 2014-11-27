package org.drugis.trialverse.dataset.repository;

import com.hp.hpl.jena.rdf.model.Model;
import org.apache.http.HttpResponse;
import org.drugis.trialverse.security.Account;

import java.security.Principal;

/**
 * Created by daan on 7-11-14.
 */
public interface DatasetReadRepository {

  public HttpResponse queryDatasets(Account currentUserAccount);

  public Model getDataset(String datasetUUID);

  public HttpResponse queryDatasetsWithDetail(String datasetUUID);

  public boolean isOwner(String datasetUUID, Principal principal);

  public boolean containsStudyWithShortname(String datasetUUID, String shortName);

}

package org.drugis.trialverse.dataset.repository;

import com.hp.hpl.jena.rdf.model.Model;
import org.apache.http.Header;
import org.apache.http.HttpResponse;

import org.drugis.trialverse.security.Account;

import java.io.IOException;
import java.net.URI;
import java.security.Principal;

/**
 * Created by daan on 7-11-14.
 */
public interface DatasetReadRepository {
  String QUERY_ENDPOINT = "/query";
  String HISTORY_ENDPOINT = "/history";
  String DATA_ENDPOINT = "/data";
  String QUERY_PARAM_QUERY = "query";
  String QUERY_STRING_DEFAULT_GRAPH = "?default";
  String VERSION_PATH = "versions/";

  public Model queryDatasets(Account currentUserAccount);

  public Boolean isOwner(URI trialverseDatasetUri, Principal principal);

  public Boolean containsStudyWithShortname(URI trialverseDatasetUri, String shortName);

  public byte[] getHistory(URI trialverseDatasetUri) throws IOException;

  public Model getVersionedDataset(URI trialverseDatasetUri, String versionUuid);

  public byte[] executeQuery(String query, URI trialverseDatasetUri, String versionUuid, String acceptHeader) throws IOException;
}

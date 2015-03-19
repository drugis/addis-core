package org.drugis.trialverse.dataset.repository;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.Model;
import org.apache.http.HttpResponse;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonObject;
import org.drugis.trialverse.security.Account;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.net.URI;
import java.security.Principal;

/**
 * Created by daan on 7-11-14.
 */
public interface DatasetReadRepository {

  public Model queryDatasets(Account currentUserAccount);

  public Model getDataset(URI trialverseDatasetUri);

  public HttpResponse queryStudiesWithDetail(URI trialverseDatasetUri) throws IOException;

  public Boolean isOwner(URI trialverseDatasetUri, Principal principal);

  public Boolean containsStudyWithShortname(URI trialverseDatasetUri, String shortName);

  public HttpResponse queryConcepts(String datasetUUID);
}

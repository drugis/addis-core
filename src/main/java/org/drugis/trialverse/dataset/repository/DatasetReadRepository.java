package org.drugis.trialverse.dataset.repository;

import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.security.Account;

import java.io.IOException;
import java.net.URI;
import java.security.Principal;

/**
 * Created by daan on 7-11-14.
 */
public interface DatasetReadRepository {

  public Model queryDatasets(Account currentUserAccount);

  public Model queryDataset(VersionMapping mapping);

  public Boolean isOwner(URI trialverseDatasetUri, Principal principal);

  public Boolean containsStudyWithShortname(URI trialverseDatasetUri, String shortName);

  public Model getHistory(URI trialverseDatasetUri) throws IOException;

  public Model getVersionedDataset(URI trialverseDatasetUri, String versionUuid);

  public byte[] executeQuery(String query, URI trialverseDatasetUri, String versionUuid, String acceptHeader) throws IOException;

  public void copyGraph(URI targetDataset, URI targetGraph, URI sourceRevision);
}

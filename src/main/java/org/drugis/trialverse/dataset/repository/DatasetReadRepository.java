package org.drugis.trialverse.dataset.repository;

import net.minidev.json.JSONObject;
import org.apache.jena.rdf.model.Model;
import org.drugis.addis.security.Account;
import org.drugis.trialverse.dataset.model.VersionMapping;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;

/**
 * Created by daan on 7-11-14.
 */
public interface DatasetReadRepository {

  Model queryDatasets(Account currentUserAccount);

  Model queryDataset(VersionMapping mapping);

  Boolean isOwner(URI trialverseDatasetUri, Principal principal);

  Boolean containsStudyWithShortname(URI trialverseDatasetUri, String shortName);

  Model getHistory(URI trialverseDatasetUri) throws IOException;

  Model getVersionedDataset(URI trialverseDatasetUri, String versionUuid);

  byte[] executeQuery(String query, URI trialverseDatasetUri, String versionUuid, String acceptHeader) throws IOException;

  JSONObject executeHeadQuery(String sparqlQuery, VersionMapping versionMapping) throws URISyntaxException;

  void copyGraph(URI targetDataset, URI targetGraph, URI sourceRevision);
}

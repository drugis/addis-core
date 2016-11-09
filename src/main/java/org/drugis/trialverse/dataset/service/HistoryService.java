package org.drugis.trialverse.dataset.service;


import org.drugis.trialverse.dataset.exception.RevisionNotFoundException;
import org.drugis.trialverse.dataset.model.VersionNode;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public interface HistoryService {
  List<VersionNode> createHistory(URI trialverseDatasetUri) throws URISyntaxException, IOException, RevisionNotFoundException;
  List<VersionNode> createHistory(URI trialverseDatasetUri, URI trialverseGraphURI) throws URISyntaxException, IOException, RevisionNotFoundException;
}

package org.drugis.addis.trialverse.service;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

/**
 * Created by connor on 25-3-14.
 */
public interface TrialverseService {

  List<ObjectNode> getTrialData(String namespaceUId, String version, String semanticOutcomeUri, List<String> alternativeUris, List<String> covariateKeys);
}

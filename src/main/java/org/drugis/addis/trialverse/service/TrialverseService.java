package org.drugis.addis.trialverse.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.drugis.addis.trialverse.service.impl.ReadValueException;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Set;

/**
 * Created by connor on 25-3-14.
 */
public interface TrialverseService {

  List<ObjectNode> getTrialData(String namespaceUId, URI version, URI semanticOutcomeUri, Set<URI> alternativeUris, Set<String> covariateKeys) throws ReadValueException, IOException;
}

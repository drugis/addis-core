package org.drugis.addis.trialverse.service;

import org.drugis.addis.trialverse.model.TrialData;

import java.util.List;

/**
 * Created by connor on 9-5-14.
 */
public interface TrialverseDataService {
  TrialData getTrialData(Long namespaceId, String outcomeUri);

  TrialData getTrialData(Long namespaceId);

  TrialData getTrialData(Long namespaceId, List<String> interventionUris);

  TrialData getTrialData(Long namespaceId, String outcomeUri, List<String> interventionUris);
}

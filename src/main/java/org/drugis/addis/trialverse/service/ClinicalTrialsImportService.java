package org.drugis.addis.trialverse.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.drugis.addis.trialverse.service.impl.ClinicalTrialsImportError;

/**
 * Created by connor on 12-5-16.
 */
public interface ClinicalTrialsImportService {
   JsonNode fetchInfo(String nctId) throws ClinicalTrialsImportError;
}

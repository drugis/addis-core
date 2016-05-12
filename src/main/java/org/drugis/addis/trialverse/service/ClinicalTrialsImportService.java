package org.drugis.addis.trialverse.service;

import net.minidev.json.JSONObject;

/**
 * Created by connor on 12-5-16.
 */
public interface ClinicalTrialsImportService {
   JSONObject fetchInfo(String ntcId);
}

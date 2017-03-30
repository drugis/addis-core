package org.drugis.addis.patavitask.service;

import org.drugis.addis.exception.ProblemCreationException;
import org.drugis.addis.interventions.service.impl.InvalidTypeForDoseCheckException;
import org.drugis.addis.patavitask.PataviTaskUriHolder;
import org.drugis.addis.trialverse.service.impl.ReadValueException;
import org.json.JSONObject;

/**
 * Created by connor on 26-6-14.
 */
public interface PataviTaskService {
  PataviTaskUriHolder getGemtcPataviTaskUriHolder(Integer projectId, Integer analysisId, Integer modelId) throws Exception, ReadValueException, InvalidTypeForDoseCheckException, ProblemCreationException;

  PataviTaskUriHolder getMcdaPataviTaskUriHolder(JSONObject problem);
}

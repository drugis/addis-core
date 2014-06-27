package org.drugis.addis.models.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.models.PataviTaskUriHolder;

/**
 * Created by connor on 26-6-14.
 */
public interface PataviTaskService {
  public PataviTaskUriHolder getPataviTaskUriHolder(Integer projectId, Integer analysisId, Integer modelId) throws ResourceDoesNotExistException, JsonProcessingException;
}

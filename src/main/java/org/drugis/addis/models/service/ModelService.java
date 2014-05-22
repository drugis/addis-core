package org.drugis.addis.models.service;

import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.models.Model;

/**
 * Created by daan on 22-5-14.
 */
public interface ModelService {
  public Model createModel(Integer projectId, Integer analysisId) throws ResourceDoesNotExistException;
}

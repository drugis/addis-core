package org.drugis.addis.analyses.service;

import org.drugis.addis.exception.ResourceDoesNotExistException;

/**
 * Created by daan on 22-5-14.
 */
public interface AnalysisService {
  public void checkCoordinates(Integer projectId, Integer analysisId) throws ResourceDoesNotExistException;
}

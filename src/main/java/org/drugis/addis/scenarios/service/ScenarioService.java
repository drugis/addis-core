package org.drugis.addis.scenarios.service;

import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.scenarios.Scenario;

/**
 * Created by connor on 16-4-14.
 */
public interface ScenarioService {
  public void checkCoordinates(Integer projectId, Integer analysisId, Scenario scenario) throws ResourceDoesNotExistException;

}

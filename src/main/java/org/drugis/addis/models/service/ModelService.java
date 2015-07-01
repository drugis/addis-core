package org.drugis.addis.models.service;

import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.models.Model;

import java.util.List;

/**
 * Created by daan on 22-5-14.
 */
public interface ModelService {
  public Model createModel(Integer projectId, Integer analysisId, String modelTitle) throws ResourceDoesNotExistException;

  public Model getModel(Integer analysisId, Integer modelId) throws ResourceDoesNotExistException;

  public List<Model> query(Integer analysisId);
}

package org.drugis.addis.models.service;

import net.minidev.json.JSONObject;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.models.Model;
import org.drugis.addis.models.exceptions.InvalidModelTypeException;

import java.util.List;

/**
 * Created by daan on 22-5-14.
 */
public interface ModelService {
  public Model createModel(Integer projectId, Integer analysisId, String modelTitle, String linearModel, String modelType, String from, String to,
                           Integer burnInIterations, Integer inferenceIterations, Integer thinningFactor) throws ResourceDoesNotExistException, InvalidModelTypeException;

  public Model getModel(Integer analysisId, Integer modelId) throws ResourceDoesNotExistException;

  public List<Model> query(Integer analysisId);
}

package org.drugis.addis.models.service;

import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.models.Model;
import org.drugis.addis.models.CreateModelCommand;
import org.drugis.addis.models.UpdateModelCommand;
import org.drugis.addis.models.exceptions.InvalidModelTypeException;

import java.security.Principal;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by daan on 22-5-14.
 */
public interface ModelService {
  public Model createModel(Integer analysisId, CreateModelCommand createModelCommand) throws ResourceDoesNotExistException, InvalidModelTypeException;

  public Model getModel(Integer analysisId, Integer modelId) throws ResourceDoesNotExistException;

  public List<Model> query(Integer analysisId) throws SQLException;

  public void checkOwnership(Integer modelId, Principal principal);

  public void increaseRunLength(UpdateModelCommand updateModelCommand);
}

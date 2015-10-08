package org.drugis.addis.models.service;

import org.drugis.addis.exception.MethodNotAllowedException;
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
  Model createModel(Integer analysisId, CreateModelCommand createModelCommand) throws ResourceDoesNotExistException, InvalidModelTypeException;

  List<Model> query(Integer analysisId) throws SQLException;

  void checkOwnership(Integer modelId, Principal principal) throws ResourceDoesNotExistException, MethodNotAllowedException;

  void increaseRunLength(UpdateModelCommand updateModelCommand) throws MethodNotAllowedException, InvalidModelTypeException;
}

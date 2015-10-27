package org.drugis.addis.models.service;

import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.models.Model;
import org.drugis.addis.models.controller.command.ModelCommand;
import org.drugis.addis.models.exceptions.InvalidHeterogeneityTypeException;
import org.drugis.addis.models.exceptions.InvalidModelTypeException;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by daan on 22-5-14.
 */
public interface ModelService {
  Model createModel(Integer analysisId, ModelCommand modelCommand) throws ResourceDoesNotExistException, InvalidModelTypeException, InvalidHeterogeneityTypeException;

  Model getModel(Integer analysisId, Integer modelId) throws ResourceDoesNotExistException;

  List<Model> query(Integer analysisId) throws SQLException;
}

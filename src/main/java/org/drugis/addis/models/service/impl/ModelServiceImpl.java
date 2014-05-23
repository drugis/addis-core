package org.drugis.addis.models.service.impl;

import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.models.Model;
import org.drugis.addis.models.repositories.ModelRepository;
import org.drugis.addis.models.service.ModelService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * Created by daan on 22-5-14.
 */
@Service
public class ModelServiceImpl implements ModelService {
  @Inject
  ModelRepository modelRepository;

  @Override
  public Model createModel(Integer projectId, Integer analysisId) throws ResourceDoesNotExistException {
    return modelRepository.create(analysisId);
  }
}

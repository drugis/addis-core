package org.drugis.addis.models.service.impl;

import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.models.Model;
import org.drugis.addis.models.repository.ModelRepository;
import org.drugis.addis.models.service.ModelService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by daan on 22-5-14.
 */
@Service
public class ModelServiceImpl implements ModelService {
  @Inject
  ModelRepository modelRepository;

  @Override
  public Model createModel(Integer projectId, Integer analysisId, String modelTitle, String linearModel) throws ResourceDoesNotExistException {
    return modelRepository.create(analysisId, modelTitle, linearModel);
  }

  @Override
  public Model getModel(Integer analysisId, Integer modelId) throws ResourceDoesNotExistException {
    return modelRepository.find(modelId);
  }

  @Override
  public List<Model> query(Integer analysisId) {
    return modelRepository.findByAnalysis(analysisId);
  }
}

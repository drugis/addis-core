package org.drugis.addis.analyses.service.impl;

import org.drugis.addis.analyses.AbstractAnalysis;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * Created by daan on 22-5-14.
 */
@Service
public class AnalysisServiceImpl implements AnalysisService {

  @Inject
  AnalysisRepository analysisRepository;

  @Override
  public void checkCoordinates(Integer projectId, Integer analysisId) throws ResourceDoesNotExistException {
    AbstractAnalysis analysis = analysisRepository.get(projectId, analysisId);
    if(!analysis.getProjectId().equals(projectId)) {
      throw new ResourceDoesNotExistException();
    }
  }
}

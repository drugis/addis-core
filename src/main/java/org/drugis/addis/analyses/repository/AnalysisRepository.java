package org.drugis.addis.analyses.repository;

import org.drugis.addis.analyses.AbstractAnalysis;
import org.drugis.addis.exception.ResourceDoesNotExistException;

import java.util.List;

/**
 * Created by daan on 7-5-14.
 */
public interface AnalysisRepository {
  AbstractAnalysis get(Integer analysisId) throws ResourceDoesNotExistException;

  List<AbstractAnalysis> query(Integer projectId);

}

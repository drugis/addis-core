package org.drugis.addis.models.repository;

import org.drugis.addis.models.Model;
import org.drugis.addis.models.exceptions.InvalidModelTypeException;

import java.util.List;

/**
 * Created by daan on 22-5-14.
 */
public interface ModelRepository {

  public Model persist(Model model) throws InvalidModelTypeException;

  public Model find(Integer modelId);

  public List<Model> findByAnalysis(Integer networkMetaAnalysisId);
}

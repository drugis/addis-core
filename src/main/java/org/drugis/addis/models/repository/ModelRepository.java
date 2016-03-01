package org.drugis.addis.models.repository;

import org.drugis.addis.models.Model;
import org.drugis.addis.models.exceptions.InvalidModelTypeException;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by daan on 22-5-14.
 */
public interface ModelRepository {

  Model persist(Model model) throws InvalidModelTypeException;

  Model find(Integer modelId);

  Model get(Integer modelId);

  List<Model> findByAnalysis(Integer networkMetaAnalysisId) throws SQLException;

  List<Model> findNetworkModelsByProject(Integer projectId);
}

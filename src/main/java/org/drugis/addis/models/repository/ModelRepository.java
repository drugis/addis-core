package org.drugis.addis.models.repository;

import org.drugis.addis.models.Model;
import org.drugis.addis.models.exceptions.InvalidModelException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by daan on 22-5-14.
 */
public interface ModelRepository {

  Model persist(Model model) throws InvalidModelException;

  Model find(Integer modelId) throws IOException;

  Model get(Integer modelId) throws IOException;

  List<Model> get(List<Integer> modelIds);

  List<Model> findByAnalysis(Integer networkMetaAnalysisId) throws SQLException;

  List<Model> findNetworkModelsByProject(Integer projectId) throws SQLException;

  void setArchived(Integer modelId, Boolean archived);
}

package org.drugis.addis.models.repository;

import org.drugis.addis.models.Model;
import org.drugis.addis.models.exceptions.InvalidModelException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

/**
 * Created by daan on 22-5-14.
 */
public interface ModelRepository {

  Model persist(Model model) throws InvalidModelException;

  Model find(Integer modelId) throws IOException;

  Model get(Integer modelId) throws IOException;

  List<Model> get(Set<Integer> modelIds);

  List<Model> findByAnalysis(Integer networkMetaAnalysisId) throws SQLException;

  List<Model> findModelsByProject(Integer projectId) throws SQLException;

  void setArchived(Integer modelId, Boolean archived);

  void setTitle(Integer modelId, String title);
}

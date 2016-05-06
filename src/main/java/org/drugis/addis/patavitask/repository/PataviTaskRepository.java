package org.drugis.addis.patavitask.repository;

import com.fasterxml.jackson.databind.JsonNode;
import org.drugis.addis.models.Model;
import org.drugis.addis.patavitask.PataviTask;
import org.drugis.addis.problems.model.NetworkMetaAnalysisProblem;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by connor on 26-6-14.
 */

public interface PataviTaskRepository {

  PataviTask get(String id);

  PataviTask createPataviTask(NetworkMetaAnalysisProblem problem, Model model) throws IOException, SQLException;

  List<PataviTask> findByIds(List<String> taskIds) throws SQLException;

  void delete(String id);

  JsonNode getResult(String taskId) throws IOException, UnexpectedNumberOfResultsException;

  Map<Integer, JsonNode> getResults(List<String> taskIds) throws SQLException, IOException;
}

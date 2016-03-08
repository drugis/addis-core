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

  PataviTask get(Integer id);

  PataviTask createPataviTask(NetworkMetaAnalysisProblem problem, Model model) throws IOException, SQLException;

  List<PataviTask> findByIds(List<Integer> taskIds) throws SQLException;

  void delete(Integer id);

  JsonNode getResult(Integer taskId) throws IOException, UnexpectedNumberOfResultsException;

  Map<Integer, JsonNode> getResults(List<Integer> taskIds) throws SQLException, IOException;
}

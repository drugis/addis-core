package org.drugis.addis.patavitask.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.drugis.addis.models.Model;
import org.drugis.addis.patavitask.PataviTask;
import org.drugis.addis.problems.model.NetworkMetaAnalysisProblem;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by connor on 26-6-14.
 */

public interface PataviTaskRepository {

  public PataviTask get(Integer id);

  public PataviTask createPataviTask(NetworkMetaAnalysisProblem problem, Model model) throws JsonProcessingException, IOException, SQLException;

  public List<PataviTask> findByIds(List<Integer> ids) throws SQLException;
}

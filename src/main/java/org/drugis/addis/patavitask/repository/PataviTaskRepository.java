package org.drugis.addis.patavitask.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.drugis.addis.patavitask.PataviTask;
import org.drugis.addis.problems.model.NetworkMetaAnalysisProblem;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by connor on 26-6-14.
 */

public interface PataviTaskRepository {

  public PataviTask createPataviTask(NetworkMetaAnalysisProblem problem) throws JsonProcessingException, IOException, SQLException;
}

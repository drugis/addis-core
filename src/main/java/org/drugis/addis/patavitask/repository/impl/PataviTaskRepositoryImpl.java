package org.drugis.addis.patavitask.repository.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.drugis.addis.models.Model;
import org.drugis.addis.patavitask.PataviTask;
import org.drugis.addis.patavitask.repository.PataviTaskRepository;
import org.drugis.addis.problems.model.NetworkMetaAnalysisProblem;
import org.json.JSONObject;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by connor on 26-6-14.
 */
@Repository
public class PataviTaskRepositoryImpl implements PataviTaskRepository {
  final static Logger logger = LoggerFactory.getLogger(PataviTaskRepositoryImpl.class);
  private ObjectMapper objectMapper = new ObjectMapper();
  public final static String GEMTC_METHOD = "gemtc";

  @Inject
  @Qualifier("dsPataviTask")
  private DataSource dataSource;

  @Inject
  @Qualifier("jtPataviTask")
  private JdbcTemplate jdbcTemplate;

  @Inject
  private SimpleJdbcInsertPataviTaskFactory simpleJdbcInsertPataviTaskFactory;

  private RowMapper<PataviTask> rowMapper = new RowMapper<PataviTask>() {
    public PataviTask mapRow(ResultSet rs, int rowNum) throws SQLException {
      return new PataviTask(rs.getInt("id"), rs.getString("method"), rs.getString("problem"), rs.getBoolean("hasResult"));
    }
  };

  @Override
  public PataviTask createPataviTask(NetworkMetaAnalysisProblem problem, Model model) throws IOException, SQLException {
    logger.trace("PataviTaskRepositoryImpl.createPataviTask");

    SimpleJdbcInsert simpleJdbcInsert = simpleJdbcInsertPataviTaskFactory.build(jdbcTemplate);

    String problemString = objectMapper.writeValueAsString(problem);
    JSONObject jsonProblem = new JSONObject(problemString);
    jsonProblem.put("linearModel", model.getLinearModel());
    jsonProblem.put("modelType", new JSONObject(objectMapper.writeValueAsString(model.getModelType())));
    jsonProblem.put("burnInIterations", model.getBurnInIterations());
    jsonProblem.put("inferenceIterations", model.getInferenceIterations());
    jsonProblem.put("thinningFactor", model.getThinningFactor());
    jsonProblem.put("likelihood", model.getLikelihood());
    jsonProblem.put("link", model.getLink());

    if(model.getOutcomeScale() != null) {
      jsonProblem.put("outcomeScale", model.getOutcomeScale());
    }

    PGobject postgresJsonObject = new PGobject();
    postgresJsonObject.setType("json");
    postgresJsonObject.setValue(jsonProblem.toString());

    MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
    mapSqlParameterSource.addValue("problem", postgresJsonObject);
    mapSqlParameterSource.addValue("method", GEMTC_METHOD);

    final Number key = simpleJdbcInsert.executeAndReturnKey(mapSqlParameterSource);
    final Integer taskId = key.intValue();
    logger.debug("created new patavi-task with taskId = " + taskId);
    return new PataviTask(taskId, GEMTC_METHOD, jsonProblem.toString());
  }

  @Override
  public List<PataviTask> findByIds(List<Integer> ids) throws SQLException {
    String query = "SELECT id, method, problem,  result IS NOT NULL as hasResult FROM patavitask WHERE id IN(UNNEST(?)) ";

    Connection connection = dataSource.getConnection();
    final PreparedStatement statement = connection.prepareStatement(query);
    statement.setArray(1, connection.createArrayOf("int", ids.toArray()));

    List<PataviTask> result = new ArrayList<>();
    int i = 0;
    try (ResultSet rs = statement.executeQuery()) {
      while (rs.next()) {
        result.add(rowMapper.mapRow(rs, i));
        i++;
      }
    }
    return result;
  }
}

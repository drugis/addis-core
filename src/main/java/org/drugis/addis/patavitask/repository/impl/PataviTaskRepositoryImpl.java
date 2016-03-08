package org.drugis.addis.patavitask.repository.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.drugis.addis.models.Model;
import org.drugis.addis.patavitask.PataviTask;
import org.drugis.addis.patavitask.repository.PataviTaskRepository;
import org.drugis.addis.patavitask.repository.UnexpectedNumberOfResultsException;
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
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by connor on 26-6-14.
 */
@Repository
public class PataviTaskRepositoryImpl implements PataviTaskRepository {
  public final static String GEMTC_METHOD = "gemtc";
  final static Logger logger = LoggerFactory.getLogger(PataviTaskRepositoryImpl.class);
  private final static String SELECTOR_PART = "SELECT id, method, problem, result IS NOT NULL as hasResult";
  private ObjectMapper objectMapper = new ObjectMapper();
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
  public PataviTask get(Integer id) {
    return jdbcTemplate.queryForObject(SELECTOR_PART + " FROM patavitask where id = ?", new Object[]{id}, rowMapper);
  }

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
    jsonProblem.put("regressor", model.getRegressor());

    if (model.getHeterogeneityPrior() != null) {
      jsonProblem.put("heterogeneityPrior", new JSONObject(objectMapper.writeValueAsString(model.getHeterogeneityPrior())));
    }

    if (model.getSensitivity() != null) {
      jsonProblem.put("sensitivity", model.getSensitivity());
    }

    if (model.getOutcomeScale() != null) {
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
  public List<PataviTask> findByIds(List<Integer> taskIds) throws SQLException {

    // Use different query for live psql db as psql does accept a set as part of the in clause
    boolean isHsqlDrive = dataSource instanceof EmbeddedDatabase;
    String query;
    if (isHsqlDrive) {
      query = SELECTOR_PART + " FROM patavitask WHERE id IN(UNNEST(?)) ";
    } else {
      query = SELECTOR_PART + " FROM patavitask WHERE id IN(select(UNNEST(?))) ";
    }

    try (Connection connection = dataSource.getConnection()) {
      final PreparedStatement statement = connection.prepareStatement(query);
      statement.setArray(1, connection.createArrayOf("int", taskIds.toArray()));

      List<PataviTask> result = new ArrayList<>();
      int i = 0;
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        result.add(rowMapper.mapRow(rs, i));
        ++i;
      }
      return result;
    }
  }

  @Override
  public void delete(Integer id) {
    jdbcTemplate.update("DELETE FROM patavitask WHERE id = ?", id);
  }

  @Override
  public JsonNode getResult(Integer taskId) throws IOException, UnexpectedNumberOfResultsException {
    String result = jdbcTemplate.queryForObject("SELECT result FROM patavitask where id = " + taskId, String.class);
    if (result == null) {
      throw new UnexpectedNumberOfResultsException("expected was 1 but got zero results");
    }
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.readTree(result).get("results");
  }

  @Override
  public Map<Integer, JsonNode> getResults(List<Integer> taskIds) throws SQLException, IOException {
    // Use different query for live psql db as psql does accept a set as part of the in clause
    boolean isHsqlDrive = dataSource instanceof EmbeddedDatabase;
    String query;
    if (isHsqlDrive) {
      query = "SELECT id, result FROM patavitask WHERE id IN(UNNEST(?)) ";
    } else {
      query = "SELECT id, result FROM patavitask WHERE id IN(select(UNNEST(?))) ";
    }

    try (Connection connection = dataSource.getConnection()) {
      final PreparedStatement statement = connection.prepareStatement(query);
      statement.setArray(1, connection.createArrayOf("int", taskIds.toArray()));

      ObjectMapper objectMapper = new ObjectMapper();

      ResultSet rs = statement.executeQuery();
      Map<Integer, JsonNode> result = new HashMap<>();
      while (rs.next()) {
        result.put(rs.getInt("id"), objectMapper.readTree(rs.getString("result")).get("results"));
      }
      return result;
    }
  }
}

package org.drugis.addis.patavitask.repository.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONValue;
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
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by connor on 26-6-14.
 */
@Repository
public class PataviTaskRepositoryImpl implements PataviTaskRepository {
  final static Logger logger = LoggerFactory.getLogger(PataviTaskRepositoryImpl.class);
  private ObjectMapper objectMapper = new ObjectMapper();
  public final static String GEMTC_METHOD = "gemtc";

  @Inject
  @Qualifier("jtPataviTask")
  private JdbcTemplate jdbcTemplate;

  @Inject
  private SimpleJdbcInsertPataviTaskFactory simpleJdbcInsertPataviTaskFactory;

  @Override
  public PataviTask createPataviTask(NetworkMetaAnalysisProblem problem, Model model) throws IOException, SQLException {
    logger.trace("PataviTaskRepositoryImpl.createPataviTask");

    SimpleJdbcInsert simpleJdbcInsert = simpleJdbcInsertPataviTaskFactory.build(jdbcTemplate);

    String problemString = objectMapper.writeValueAsString(problem);
    JSONObject jsonProblem = new JSONObject(problemString);
    jsonProblem.put("linearModel", model.getLinearModel());
    jsonProblem.put("modelType", new JSONObject(objectMapper.writeValueAsString(model.getModelType())));

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
}

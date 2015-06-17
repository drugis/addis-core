package org.drugis.addis.patavitask.repository.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import org.drugis.addis.patavitask.PataviTask;
import org.drugis.addis.patavitask.repository.PataviTaskRepository;
import org.drugis.addis.problems.model.NetworkMetaAnalysisProblem;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.UsernameAlreadyInUseException;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
  public PataviTask createPataviTask(NetworkMetaAnalysisProblem problem) throws JsonProcessingException, IOException, SQLException {
    logger.trace("PataviTaskRepositoryImpl.createPataviTask");

    SimpleJdbcInsert simpleJdbcInsert = simpleJdbcInsertPataviTaskFactory.build(jdbcTemplate);

    PGobject jsonObject = new PGobject();
    jsonObject.setType("json");
    String problemString = objectMapper.writeValueAsString(problem);
    jsonObject.setValue(problemString);

    MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
    mapSqlParameterSource.addValue("problem", jsonObject);
    mapSqlParameterSource.addValue("method", GEMTC_METHOD);

    final Number key = simpleJdbcInsert.executeAndReturnKey(mapSqlParameterSource);
    final Integer taskId = key.intValue();
    logger.debug("created new patavi-task with taskId = " + taskId);
    return new PataviTask(taskId, GEMTC_METHOD, problemString);
  }
}

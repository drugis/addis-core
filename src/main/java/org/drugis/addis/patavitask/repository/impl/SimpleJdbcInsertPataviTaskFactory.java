package org.drugis.addis.patavitask.repository.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;

/**
 * Created by connor on 17/06/15.
 */
@Component
public class SimpleJdbcInsertPataviTaskFactory  {

  public SimpleJdbcInsert build(JdbcTemplate jdbcTemplate) {
    SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
    simpleJdbcInsert.withTableName("PataviTask")
            .usingColumns("problem", "method")
            .usingGeneratedKeyColumns("id");
    return simpleJdbcInsert;
  }
}

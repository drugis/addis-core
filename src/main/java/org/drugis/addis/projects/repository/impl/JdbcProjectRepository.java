package org.drugis.addis.projects.repository.impl;

import org.drugis.addis.projects.Project;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;

/**
 * Created by daan on 2/6/14.
 */
@Repository
public class JdbcProjectRepository implements ProjectRepository {

  @Inject
  private JdbcTemplate jdbcTemplate;

  private RowMapper<Project> rowMapper = new RowMapper<Project>() {
    public Project mapRow(ResultSet rs, int rowNum) throws SQLException {
      return new Project(rs.getInt("id"), rs.getInt("owner"), rs.getString("name"), rs.getString("description"));
    }
  };

  @Override
  public Collection<Project> query() {
    String staticSqlStatment = "SELECT id, owner, name, description FROM Project";
    return jdbcTemplate.query(staticSqlStatment, rowMapper);
  }
}

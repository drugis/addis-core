package org.drugis.addis.projects.repository.impl;

import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.projects.Outcome;
import org.drugis.addis.projects.Project;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.security.Account;
import org.drugis.addis.trialverse.Trialverse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import sun.util.logging.resources.logging;

import javax.inject.Inject;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Created by daan on 2/6/14.
 */
public class JdbcProjectRepository implements ProjectRepository {

  final static Logger logger = LoggerFactory.getLogger(JdbcProjectRepository.class);

  @Inject
  private JdbcTemplate jdbcTemplate;

  private RowMapper<Project> rowMapper = new RowMapper<Project>() {
    public Project mapRow(ResultSet rs, int rowNum) throws SQLException {
      Account owner = new Account(rs.getInt("ownerId"), rs.getString("ownerUserName"), rs.getString("ownerFirstName"), rs.getString("ownerLastName"));
      return new Project(rs.getInt("id"), owner, rs.getString("name"), rs.getString("description"), new Trialverse(rs.getString("trialverse")));
    }
  };

  @Override
  public Collection<Project> query() {
    String staticSqlStatment = "SELECT p.id, a.id ownerId," +
            " a.username ownerUserName," +
            " a.firstname ownerFirstName," +
            " a.lastname ownerLastName," +
            " p.name, p.description, p.trialverse " +
            "FROM Project p, Account a" +
            " WHERE p.owner = a.id";
    return jdbcTemplate.query(staticSqlStatment, rowMapper);
  }

  @Override
  public Project getProjectById(Integer projectId) throws ResourceDoesNotExistException {
    Assert.notNull(projectId, "project id can not ben null");
    String queryString = "SELECT p.id, a.id ownerId," +
            " a.username ownerUserName," +
            " a.firstname ownerFirstName," +
            " a.lastname ownerLastName," +
            " p.name, p.description, p.trialverse " +
            "FROM Project p, Account a" +
            " WHERE p.owner = a.id AND p.id = ?";
    try {
      return jdbcTemplate.queryForObject(queryString, rowMapper, projectId);
    } catch (DataAccessException e) {
      logger.error(e.toString());
      throw new ResourceDoesNotExistException();
    }
  }

  @Override
  public Collection<Project> queryByOwnerId(Integer ownerId) {
    String queryString = "SELECT p.id, a.id ownerId," +
            " a.username ownerUserName," +
            " a.firstname ownerFirstName," +
            " a.lastname ownerLastName," +
            " p.name, p.description, p.trialverse " +
            "FROM Project p, Account a" +
            " WHERE p.owner = ? AND p.owner = a.id";
    PreparedStatementCreatorFactory pscf = new PreparedStatementCreatorFactory(queryString);
    pscf.addParameter(new SqlParameter(Types.INTEGER));
    return jdbcTemplate.query(
            pscf.newPreparedStatementCreator(new Object[]{ownerId}), rowMapper);
  }

  @Override
  public Project create(final Account owner, final String name, final String description, final Trialverse trialverse) {
    KeyHolder keyHolder = new GeneratedKeyHolder();

    jdbcTemplate.update(new PreparedStatementCreator() {

      @Override
      public PreparedStatement createPreparedStatement(Connection connection)
              throws SQLException {
        PreparedStatement ps = connection.prepareStatement("insert into Project (owner, name, description, trialverse) values (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, owner.getId());
        ps.setString(2, name);
        ps.setString(3, description);
        ps.setString(4, trialverse.getName());
        return ps;
      }
    }, keyHolder);

    Integer id = (Integer) keyHolder.getKeys().get("ID");
    return new Project(id, owner, name, description, trialverse);
  }


}


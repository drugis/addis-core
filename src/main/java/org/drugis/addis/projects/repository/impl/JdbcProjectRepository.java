package org.drugis.addis.projects.repository.impl;

import org.drugis.addis.projects.Project;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.security.Account;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.sql.*;
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
      Account owner = new Account(rs.getInt("ownerId"), rs.getString("ownerUserName"), rs.getString("ownerFirstName"), rs.getString("ownerLastName"));
      return new Project(rs.getInt("id"), owner, rs.getString("name"), rs.getString("description"), rs.getString("namespace"));
    }
  };

  @Override
  public Collection<Project> query() {
    String staticSqlStatment = "SELECT p.id, a.id ownerId," +
            " a.username ownerUserName," +
            " a.firstname ownerFirstName," +
            " a.lastname ownerLastName," +
            " p.name, p.description, p.namespace " +
            "FROM Project p, Account a" +
            " WHERE p.owner = a.id";
    return jdbcTemplate.query(staticSqlStatment, rowMapper);
  }

  @Override
  public Collection<Project> queryByOwnerId(Integer ownerId) {
    String queryString = "SELECT p.id, a.id ownerId," +
            " a.username ownerUserName," +
            " a.firstname ownerFirstName," +
            " a.lastname ownerLastName," +
            " p.name, p.description, p.namespace " +
            "FROM Project p, Account a" +
            " WHERE p.owner = ? AND p.owner = a.id";
    PreparedStatementCreatorFactory pscf = new PreparedStatementCreatorFactory(queryString);
    pscf.addParameter(new SqlParameter(Types.INTEGER));
    return jdbcTemplate.query(
            pscf.newPreparedStatementCreator(new Object[] { ownerId }), rowMapper);
  }

  @Override
  public Project create(final Account owner, final String name, final String description, final String namespace) {
    		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(new PreparedStatementCreator() {
			@Override
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				PreparedStatement ps = con.prepareStatement("insert into Project (owner, name, description, namespace) values (?, ?, ?, ?)",
            new String[] {"id"});
				ps.setInt(1, owner.getId());
				ps.setString(2, name);
				ps.setString(3, description);
        ps.setString(4, namespace);
				return ps;
			}
		}, keyHolder);
		int projectId = (Integer) keyHolder.getKey();
		return new Project(projectId, owner, name, description, namespace);
  }


}

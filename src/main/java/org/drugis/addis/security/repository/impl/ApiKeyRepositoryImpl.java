package org.drugis.addis.security.repository.impl;

import org.drugis.addis.security.ApiKey;
import org.drugis.addis.security.repository.ApiKeyRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by daan on 15-9-15.
 */
@Repository
public class ApiKeyRepositoryImpl implements ApiKeyRepository {
  @Inject
  @Qualifier("jtAddisCore")
  private JdbcTemplate jdbcTemplate;

  private RowMapper<ApiKey> rowMapper = new RowMapper<ApiKey>() {
    public ApiKey mapRow(ResultSet rs, int rowNum) throws SQLException {
      return new ApiKey(rs.getInt("id"),
              rs.getString("secretKey"),
              rs.getInt("accountId"),
              rs.getString("applicationName"),
              rs.getDate("creationDate"),
              rs.getDate("revocationDate"));
    }
  };

  @Override
  public ApiKey getKeyBySecretCode(String secretCode) {
    return jdbcTemplate.queryForObject(
            "SELECT id, secretKey, accountId, applicationname, creationDate, revocationDate " +
                    "FROM ApplicationKey where secretkey = ?", rowMapper, secretCode);

  }

  @Override
  public ApiKey get(Integer id) {
    return jdbcTemplate.queryForObject(
            "SELECT id, secretKey, accountId, applicationname, creationDate, revocationDate " +
                    "FROM ApplicationKey where id = ?", rowMapper, id);

  }
}

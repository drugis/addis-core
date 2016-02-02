package org.drugis.trialverse.dataset.repository.impl;

import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by connor on 10-3-15.
 */
@Repository
public class VersionMappingRepositoryImpl implements VersionMappingRepository {

  private final static Logger logger = LoggerFactory.getLogger(DatasetWriteRepositoryImpl.class);

  private RowMapper<VersionMapping> rowMapper = new RowMapper<VersionMapping>() {
    public VersionMapping mapRow(ResultSet rs, int rowNum) throws SQLException {
      return new VersionMapping(rs.getInt("id"), rs.getString("versionedDatasetUrl"), rs.getString("ownerUuid"), rs.getString("trialverseDatasetUrl"));
    }
  };

  @Inject
  @Qualifier("jtAddisCore")
  private JdbcTemplate jdbcTemplate;

  @Override
  public void save(VersionMapping versionMapping) {
    try {
      jdbcTemplate.update("insert into VersionMapping (versionedDatasetUrl, ownerUuid, trialverseDatasetUrl) values (?, ?, ?)",
              versionMapping.getVersionedDatasetUrl(), versionMapping.getOwnerUuid(), versionMapping.getTrialverseDatasetUrl());
    } catch (DuplicateKeyException e) {
      logger.error("duplicate mapping key");
      throw new RuntimeException("duplicate mapping key");
    }
  }

  @Override
  public List<VersionMapping> findMappingsByEmail(String email) {
    String sql = "SELECT * FROM VersionMapping WHERE ownerUuid = ?";
    List<VersionMapping> queryResult = jdbcTemplate.query(sql, Arrays.asList(email).toArray(), rowMapper);
    if (queryResult == null) {
      queryResult = new ArrayList<VersionMapping>();
    }
    return queryResult;
  }

  @Override
  public List<VersionMapping> getVersionMappings() {
    String sql = "SELECT * FROM VersionMapping";
    Object[] noArgs = {};
    List<VersionMapping> queryResult = jdbcTemplate.query(sql, noArgs, rowMapper);
    if (queryResult == null) {
      queryResult = new ArrayList<VersionMapping>();
    }
    return queryResult;
  }


  @Override
  public VersionMapping getVersionMappingByDatasetUrl(URI trialverseDatsetUrl) {
    String sql = "Select * FROM VersionMapping WHERE trialverseDatasetUrl = ?";
    return jdbcTemplate.queryForObject(sql, rowMapper, trialverseDatsetUrl.toString());
  }

  @Override
  public VersionMapping getVersionMappingByVersionedURl(URI versionedUri) {
    String sql = "Select * FROM VersionMapping WHERE versioneddataseturl = ?";
    return jdbcTemplate.queryForObject(sql, rowMapper, versionedUri.toString());
  }
}

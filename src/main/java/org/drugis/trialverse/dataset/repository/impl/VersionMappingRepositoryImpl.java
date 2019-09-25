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
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
public class VersionMappingRepositoryImpl implements VersionMappingRepository {


  @Qualifier("emAddisCore")
  @PersistenceContext(unitName = "addisCore")
  EntityManager em;

  private final static Logger logger = LoggerFactory.getLogger(DatasetWriteRepositoryImpl.class);

  private RowMapper<VersionMapping> rowMapper = (rs, rowNum) -> new VersionMapping(
          rs.getInt("id"),
          rs.getString("versionedDatasetUrl"),
          rs.getString("ownerUuid"),
          rs.getString("trialverseDatasetUrl"),
          rs.getBoolean("archived"),
          rs.getString("archivedOn")
  );

  @Inject
  @Qualifier("jtAddisCore")
  private JdbcTemplate jdbcTemplate;

  @Override
  public void save(VersionMapping versionMapping) {
    try {
      jdbcTemplate.update(
              "insert into VersionMapping (versionedDatasetUrl, ownerUuid, trialverseDatasetUrl) values (?, ?, ?)",
              versionMapping.getVersionedDatasetUrl(),
              versionMapping.getOwnerUuid(),
              versionMapping.getTrialverseDatasetUrl()
      );
    } catch (DuplicateKeyException e) {
      logger.error("duplicate mapping key");
      throw new RuntimeException("duplicate mapping key");
    }
  }

  @Override
  public List<VersionMapping> findMappingsByEmail(String email) {
    String sql = "SELECT * FROM VersionMapping WHERE ownerUuid = ?";
    List<VersionMapping> queryResult = jdbcTemplate.query(sql, Collections.singletonList(email).toArray(), rowMapper);
    if (queryResult == null) {
      queryResult = new ArrayList<>();
    }
    return queryResult;
  }

  @Override
  public List<VersionMapping> findMappingsByTrialverseDatasetUrls(List<String> datasetUrls) {
    if (datasetUrls.isEmpty()) {
      return Collections.emptyList();
    }
    TypedQuery<VersionMapping> query = em.createQuery(
            "FROM VersionMapping WHERE trialverseDatasetUrl IN (:datasetUrls)"
            , VersionMapping.class
    );
    query.setParameter("datasetUrls", datasetUrls);
    return query.getResultList();
  }

  @Override
  public List<VersionMapping> getVersionMappings() {
    String sql = "SELECT * FROM VersionMapping";
    Object[] noArgs = {};
    List<VersionMapping> queryResult = jdbcTemplate.query(sql, noArgs, rowMapper);
    if (queryResult == null) {
      queryResult = new ArrayList<>();
    }
    return queryResult;
  }

  @Override
  public VersionMapping getVersionMappingByDatasetUrl(URI trialverseDatasetUrl) {
    String sql = "Select * FROM VersionMapping WHERE trialverseDatasetUrl = ?";
    return jdbcTemplate.queryForObject(sql, rowMapper, trialverseDatasetUrl.toString());
  }

  @Override
  public VersionMapping getVersionMappingByVersionedURl(URI versionedUri) {
    String sql = "Select * FROM VersionMapping WHERE versionedDatasetUrl = ?";
    return jdbcTemplate.queryForObject(sql, rowMapper, versionedUri.toString());
  }

  @Override
  public void setArchivedStatus(URI datasetUri, Boolean archived) {
    String date = getDate(archived);
    String query = "UPDATE VersionMapping SET archived=?, archivedOn=" + date + " WHERE trialverseDatasetUrl=?";
    jdbcTemplate.update(query, archived, datasetUri.toString());
  }

  private String getDate(Boolean archived) {
    if (archived) {
      return "to_char(NOW(), 'YYYY-MM-DD')";
    } else {
      return "NULL";
    }
  }
}

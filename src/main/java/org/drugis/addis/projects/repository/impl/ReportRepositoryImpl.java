package org.drugis.addis.projects.repository.impl;

import org.drugis.addis.projects.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;

@Repository
public class ReportRepositoryImpl implements ReportRepository {

  @Inject
  @Qualifier("jtAddisCore")
  private JdbcTemplate jdbcTemplate;

  @Override
  public String get(Integer projectId) {
    String query = "select text from customReport where projectId = ?";
    return jdbcTemplate.queryForObject(query, new Integer[]{projectId}, String.class);
  }

  @Override
  public void update(Integer projectId, String newReport) {
    try {
      get(projectId);
      String query = "update customReport set text = ? where projectId = ?";
      jdbcTemplate.update(query, newReport, projectId);
    } catch (EmptyResultDataAccessException e) {
      String query = "insert into customReport(projectId, text) values (?, ?)";
      jdbcTemplate.update(query, projectId, newReport);
    }
  }

  @Override
  public String delete(Integer projectId) {
    update(projectId, "default report text");
    return get(projectId);
  }
}

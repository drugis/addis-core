package org.drugis.addis.projects.repository;

/**
 * Created by daan on 23-11-16.
 */
public interface ReportRepository {

  String get(Integer projectId);

  void update(Integer projectId, String newReport);

  String delete(Integer projectId);
}

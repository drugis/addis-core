package org.drugis.addis.projects.repository.impl;

import org.drugis.addis.projects.Project;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

/**
 * Created by daan on 2/6/14.
 */
@Repository
public class JdbcProjectRepository implements ProjectRepository {
  @Override
  public Collection<Project> query() {
    return null;
  }
}

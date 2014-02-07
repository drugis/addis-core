package org.drugis.addis.projects.repository;

import org.drugis.addis.projects.Project;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;

/**
 * Created by daan on 2/6/14.
 */
public interface ProjectRepository {
  Collection<Project> query();
  Collection<Project> queryByOwnerId(Integer ownerId);
}

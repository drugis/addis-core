package org.drugis.addis.projects.repository;

import org.drugis.addis.projects.Project;

import java.util.Collection;

/**
 * Created by daan on 2/6/14.
 */
public interface ProjectRepository {
  Collection<Project> query();
}

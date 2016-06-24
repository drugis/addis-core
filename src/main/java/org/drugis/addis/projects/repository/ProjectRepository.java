package org.drugis.addis.projects.repository;

import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.projects.Project;
import org.drugis.addis.projects.ProjectCommand;
import org.drugis.addis.security.Account;

import java.util.Collection;

/**
 * Created by daan on 2/6/14.
 */
public interface ProjectRepository {
  Collection<Project> query();

  Project get(Integer projectId) throws ResourceDoesNotExistException;

  Collection<Project> queryByOwnerId(Integer ownerId);

  Project create(Account user, ProjectCommand command);

  Boolean isExistingProjectName(Integer projectId, String name);

  Project updateNameAndDescription(Integer projectId, String name, String description) throws ResourceDoesNotExistException;
}
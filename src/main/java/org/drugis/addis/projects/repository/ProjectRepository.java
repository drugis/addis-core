package org.drugis.addis.projects.repository;

import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.projects.Project;
import org.drugis.addis.security.Account;
import org.drugis.addis.trialverse.Trialverse;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;

/**
 * Created by daan on 2/6/14.
 */
public interface ProjectRepository {
  Collection<Project> query();

  Project getProjectById(Integer projectId) throws ResourceDoesNotExistException;

  Collection<Project> queryByOwnerId(Integer ownerId);
  Project create(Account owner, String name, String description, Trialverse trialverse);

  Project update(Project body);
}
package org.drugis.addis.projects.repository;

import org.drugis.addis.projects.Project;
import org.drugis.addis.security.Account;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;

/**
 * Created by daan on 2/6/14.
 */
public interface ProjectRepository {
  Collection<Project> query();
  Collection<Project> queryByOwnerId(Integer ownerId);
  Project create(Account owner, String name, String description, String namespace);
}

package org.drugis.addis.projects.repository.impl;

import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.projects.Project;
import org.drugis.addis.projects.ProjectCommand;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.security.Account;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.Collection;

/**
 * Created by daan on 2/20/14.
 */
@Repository
public class ProjectRepositoryImpl implements ProjectRepository {

  @Qualifier("emAddisCore")
  @PersistenceContext(unitName = "addisCore")
  EntityManager em;

  @Override
  public Collection<Project> query() {
    return em.createQuery("from Project").getResultList();
  }

  @Override
  public Project get(Integer projectId) throws ResourceDoesNotExistException {
    Project project = em.find(Project.class, projectId);
    if (project == null) {
      throw new ResourceDoesNotExistException();
    }
    return project;
  }

  @Override
  public Collection<Project> queryByOwnerId(Integer ownerId) {
    TypedQuery<Project> query = em.createQuery("from Project p where p.owner.id = :ownerId", Project.class);
    query.setParameter("ownerId", ownerId);
    return query.getResultList();
  }

  @Override
  public Project create(Account user, ProjectCommand command) {
    Project project = new Project(user, command.getName(), command.getDescription(), command.getNamespaceUid(), command.getDatasetVersion());
    em.persist(project);
    return project;
  }

  @Override
  public Boolean isExistingProjectName(Integer projectId, String name) {
    TypedQuery<Project> query = em.createQuery("FROM Project p " +
            "WHERE p.id != :projectId " +
            "AND p.name LIKE :name " +
            "AND p.owner = (" +
            " SELECT p2.owner " +
            " FROM Project p2 " +
            " WHERE p2.id = :projectId" +
            ")", Project.class);
    query.setParameter("projectId", projectId);
    query.setParameter("name", name);
    return !query.getResultList().isEmpty();
  }

  @Override
  public Project updateNameAndDescription(Integer projectId, String name, String description) throws ResourceDoesNotExistException {
    Project project = em.find(Project.class, projectId);
    if (project == null) {
      throw new ResourceDoesNotExistException();
    }
    project.setName(name);
    project.setDescription(description);
    return project;
  }
}

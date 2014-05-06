package org.drugis.addis.analyses.repository.impl;

import org.drugis.addis.analyses.AnalysisCommand;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.projects.Project;
import org.drugis.addis.security.Account;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;

/**
 * Created by connor on 6-5-14.
 */
@Component
public class AnalysisRepositoryUtils {
  public void checkProjectExistsAndModifiable(Account user, AnalysisCommand analysisCommand, EntityManager em) throws ResourceDoesNotExistException, MethodNotAllowedException {
    Project project = em.find(Project.class, analysisCommand.getProjectId());
    if (project == null) {
      throw new ResourceDoesNotExistException();
    }
    if (!project.getOwner().getId().equals(user.getId())) {
      throw new MethodNotAllowedException();
    }
  }
}

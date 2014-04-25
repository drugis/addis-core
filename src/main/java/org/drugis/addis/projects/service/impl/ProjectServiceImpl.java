package org.drugis.addis.projects.service.impl;

import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.projects.Project;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.security.Principal;

/**
 * Created by connor on 16-4-14.
 */
@Service
public class ProjectServiceImpl implements ProjectService {

  @Inject
  AccountRepository accountRepository;

  @Inject
  ProjectRepository projectRepository;

  @Override
  public void checkOwnership(Integer projectId, Principal principal) throws MethodNotAllowedException, ResourceDoesNotExistException {
    Account user = accountRepository.findAccountByUsername(principal.getName());
    Project project = projectRepository.getProjectById(projectId);

    if (project == null || !project.getOwner().equals(user)) {
      throw new MethodNotAllowedException();
    }
  }
}

package org.drugis.addis.projects.service;

import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.projects.Project;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.projects.service.impl.ProjectServiceImpl;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.security.Principal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by connor on 16-4-14.
 */
public class ProjectServiceTest {

  @Mock
  AccountRepository accountRepository;

  @Mock
  ProjectRepository projectRepository;

  @InjectMocks
  ProjectService projectService;

  private Integer projectId = 1;
  private String username = "gert";
  private Principal principal = mock(Principal.class);
  private Account account = mock(Account.class);
  private Project mockProject = mock(Project.class);

  @Before
  public void setUp() throws ResourceDoesNotExistException {
    accountRepository = mock(AccountRepository.class);
    projectRepository = mock(ProjectRepository.class);
    projectService = new ProjectServiceImpl();

    initMocks(this);

    when(mockProject.getOwner()).thenReturn(account);
    when(principal.getName()).thenReturn(username);
    when(projectRepository.get(projectId)).thenReturn(mockProject);
    when(accountRepository.findAccountByUsername(username)).thenReturn(account);
  }

  @Test
  public void testCheckOwnership() throws Exception {
    projectService.checkOwnership(projectId, principal);
  }

  @Test(expected = MethodNotAllowedException.class)
  public void testOwnershipFails() throws MethodNotAllowedException, ResourceDoesNotExistException {
    Account account2 = mock(Account.class);

    when(mockProject.getOwner()).thenReturn(account2);

    projectService.checkOwnership(projectId, principal);
  }

}

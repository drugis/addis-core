package org.drugis.addis.projects.service;

import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.outcomes.repository.OutcomeRepository;
import org.drugis.addis.projects.Project;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.projects.service.impl.ProjectServiceImpl;
import org.drugis.addis.projects.service.impl.UpdateProjectException;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.drugis.addis.trialverse.service.impl.ReadValueException;
import org.drugis.addis.trialverse.service.impl.TriplestoreServiceImpl;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.access.method.P;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by connor on 16-4-14.
 */
public class ProjectServiceTest {

  @Mock
  AccountRepository accountRepository;

  @Mock
  ProjectRepository projectRepository;

  @Mock
  VersionMappingRepository versionMappingRepository;

  @Mock
  OutcomeRepository outcomeRepository;

  @InjectMocks
  ProjectService projectService;

  @InjectMocks
  TriplestoreService triplestoreService;

  private Integer projectId = 1;
  private String username = "gert";
  private Principal principal = mock(Principal.class);
  private Account account = mock(Account.class);
  private Project mockProject = mock(Project.class);
  private VersionMapping mapping = mock(VersionMapping.class);

  private static final Integer PROJECT_ID = 1;
  private static final String datasetUuid = "datasetUri1";
  private static final URI datasetUri = URI.create("http://trials.drugis.org/datasets/datasetUri1");
  private static final URI versionedDatasetUri = URI.create("datasetUuid1");

  @Before
  public void setUp() throws ResourceDoesNotExistException, URISyntaxException {
    accountRepository = mock(AccountRepository.class);
    projectRepository = mock(ProjectRepository.class);
    versionMappingRepository = mock(VersionMappingRepository.class);
    outcomeRepository = mock(OutcomeRepository.class);

    projectService = new ProjectServiceImpl();
    triplestoreService = new TriplestoreServiceImpl();

    initMocks(this);

    when(mockProject.getOwner()).thenReturn(account);
    when(mockProject.getNamespaceUid()).thenReturn(datasetUuid);
    when(principal.getName()).thenReturn(username);
    when(projectRepository.get(projectId)).thenReturn(mockProject);
    when(accountRepository.findAccountByUsername(username)).thenReturn(account);
    when(versionMappingRepository.getVersionMappingByDatasetUrl(datasetUri)).thenReturn(mapping);
    when(mapping.getVersionedDatasetUri()).thenReturn(versionedDatasetUri);
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

  @Test
  public void update() throws ResourceDoesNotExistException, UpdateProjectException {
    String name = "name";
    String description = "description";
    when(projectRepository.isExistingProjectName(projectId, name)).thenReturn(Boolean.FALSE);
    when(projectRepository.updateNameAndDescription(projectId, name, description)).thenReturn(mockProject);
    Project project = projectService.updateProject(projectId, name, description);
    assertEquals(mockProject, project);
    verify(projectRepository).isExistingProjectName(projectId, name);
    verify(projectRepository).updateNameAndDescription(projectId, name, description);
  }

  @Test(expected = UpdateProjectException.class)
  public void updateDuplicateName() throws ResourceDoesNotExistException, UpdateProjectException {
    String name = "name";
    String description = "description";
    when(projectRepository.isExistingProjectName(projectId, name)).thenReturn(Boolean.TRUE);
    projectService.updateProject(projectId, name, description);
  }

  @Test
  public void copy() throws ResourceDoesNotExistException, ReadValueException, URISyntaxException {

    projectService.copy(account, PROJECT_ID);

  }
}

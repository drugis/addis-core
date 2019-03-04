package org.drugis.addis.projects;

import org.apache.commons.lang3.StringUtils;
import org.drugis.addis.TestUtils;
import org.drugis.addis.config.TestConfig;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.projects.controller.EditProjectCommand;
import org.drugis.addis.projects.controller.ProjectController;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.projects.repository.ReportRepository;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.util.WebConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;
import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by daan on 2/6/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@WebAppConfiguration
public class ProjectsControllerTest {

  public static final String DEFAULT_REPORT_TEXT = "default report text";
  private MockMvc mockMvc;

  @Inject
  private AccountRepository accountRepository;

  @Inject
  private ProjectRepository projectRepository;

  @Mock
  private ProjectService projectService;

  @Inject
  private ReportRepository reportRepository;

  @Inject
  private WebApplicationContext webApplicationContext;

  @InjectMocks
  private ProjectController projectController;


  private Principal user;

  private Account john = new Account(1, "a", "john", "lennon", "john@apple.co"),
          paul = new Account(2, "a", "paul", "mc cartney", "paul@apple.co"),
          gert = new Account(3, "gert", "Gert", "van Valkenhoef", "gert@test.com");
  private final URI version = URI.create("http://version.com/1");


  @Before
  public void setUp() {
    reset(accountRepository);
    reset(projectRepository);
    reset(reportRepository);
    projectService = mock(ProjectService.class);
    projectController = new ProjectController();
    user = mock(Principal.class);
    initMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(projectController).build();
    when(user.getName()).thenReturn("gert");
    when(accountRepository.findAccountByUsername("gert")).thenReturn(gert);
  }


  @After
  public void tearDown() {
    verifyNoMoreInteractions(accountRepository, projectRepository);
  }

  @Test
  public void testQueryEmptyProjects() throws Exception {
    when(projectRepository.query()).thenReturn(Collections.<Project>emptyList());

    mockMvc.perform(get("/projects").principal(user))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()))
            .andExpect(jsonPath("$", hasSize(0)));

    verify(projectRepository).query();
  }

  @Test
  public void testQueryProjects() throws Exception {
    Project project = new Project(1, john, "name", "desc", "uid1", version);
    Project project2 = new Project(2, paul, "otherName", "other description", "uid2", version);
    List<Project> projects = Arrays.asList(project, project2);
    when(projectRepository.query()).thenReturn(projects);

    mockMvc.perform(get("/projects").principal(user))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()))
            .andExpect(jsonPath("$", hasSize(projects.size())))
            .andExpect(jsonPath("$[0].id", is(project.getId())))
            .andExpect(jsonPath("$[0].owner.id", is(project.getOwner().getId())))
            .andExpect(jsonPath("$[0].name", is(project.getName())))
            .andExpect(jsonPath("$[0].description", is(project.getDescription())))
            .andExpect(jsonPath("$[0].namespaceUid", is(project.getNamespaceUid())))
            .andExpect(jsonPath("$[0].datasetVersion", is(project.getDatasetVersion().toString())));

    verify(projectRepository).query();
  }

  @Test
  public void testQueryProjectsWithQueryString() throws Exception {
    Project project = new Project(2, paul, "test2", "desc", "uid1", version);
    List<Project> projects = Collections.singletonList(project);
    when(projectRepository.queryByOwnerId(paul.getId())).thenReturn(projects);

    mockMvc.perform(get("/projects?owner=2").principal(user))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()))
            .andExpect(jsonPath("$", hasSize(projects.size())))
            .andExpect(jsonPath("$[0].owner.id", is(project.getOwner().getId())));


    verify(projectRepository).queryByOwnerId(paul.getId());
  }

  @Test
  public void testCreateProject() throws Exception {
    ProjectCommand projectCommand = new ProjectCommand("testname", "testdescription", "uid1", version);
    Project project = new Project(1, gert, "testname", "testdescription", "uid1", version);
    String jsonContent = TestUtils.createJson(projectCommand);
    when(projectRepository.create(gert, projectCommand)).thenReturn(project);
    mockMvc.perform(post("/projects").principal(user).content(jsonContent).contentType(WebConstants.getApplicationJsonUtf8Value()))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()))
            .andExpect(jsonPath("$.name", is("testname")));
    verify(accountRepository).findAccountByUsername(gert.getUsername());
    verify(projectRepository).create(gert, projectCommand);
  }

  @Test
  public void testGetSingleProject() throws Exception {
    Project project = new Project(1, john, "name", "desc", "uid1", version);
    when(projectRepository.get(project.getId())).thenReturn(project);
    mockMvc.perform(get("/projects/" + project.getId()).principal(user))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()))
            .andExpect(jsonPath("$.id", is(project.getId())));
    verify(projectRepository).get(project.getId());
  }

  @Test
  public void testGetNonexistentProject() throws Exception {
    int projectId = 1;
    when(projectRepository.get(projectId)).thenThrow(new ResourceDoesNotExistException());
    mockMvc.perform(get("/projects/1").principal(user))
            .andExpect(status().isNotFound());
    verify(projectRepository).get(projectId);
  }

  @Test
  public void testHandleNullDescription() throws Exception {
    ProjectCommand projectCommand = new ProjectCommand("testname", "uid1", version);
    Project project = new Project(1, gert, "testname", StringUtils.EMPTY, "uid1", version);
    String requestBody = TestUtils.createJson(projectCommand);
    when(projectRepository.create(gert, projectCommand)).thenReturn(project);
    mockMvc.perform(post("/projects").principal(user).content(requestBody).contentType(WebConstants.getApplicationJsonUtf8Value()))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()))
            .andExpect(jsonPath("$.name", is("testname")));
    verify(accountRepository).findAccountByUsername(gert.getUsername());
    verify(projectRepository).create(gert, projectCommand);
  }

  @Test
  public void updateTitleAndDescription() throws Exception {
    EditProjectCommand command = new EditProjectCommand("updateName", "updateDescription");
    URI version = URI.create("http://version.com/1");
    Project project = new Project(1, gert, "updateName", "updateDescription", "uid1", version);
    String jsonContent = TestUtils.createJson(command);
    when(projectService.updateProject(project.getId(), command.getName(), command.getDescription())).thenReturn(project);
    mockMvc.perform(post("/projects/1").principal(user).content(jsonContent).contentType(WebConstants.getApplicationJsonUtf8Value()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()));
    verify(projectService).checkOwnership(project.getId(), user);
    verify(projectService).updateProject(project.getId(), command.getName(), command.getDescription());
  }

  @Test
  public void testGetReport() throws Exception {
    String reportContent = "report content";
    when(reportRepository.get(1)).thenReturn(reportContent);
    mockMvc.perform(get("/projects/1/report"))
            .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
            .andExpect(status().isOk())
            .andExpect(content().string(reportContent));
    verify(reportRepository).get(1);
  }

  @Test
  public void testUpdateReport() throws Exception {
    String newReport = "new report";
    mockMvc.perform(put("/projects/1/report").content(newReport))
            .andExpect(status().isOk());
    verify(reportRepository).update(1, newReport);
  }

  @Test
  public void testGetNonexistentShouldGetDefault() throws Exception {
    when(reportRepository.get(1)).thenThrow(new EmptyResultDataAccessException("Expected 1 result", 1));
    mockMvc.perform(get("/projects/1/report"))
            .andExpect(status().isOk())
            .andExpect(content().string(DEFAULT_REPORT_TEXT))
    ;
    verify(reportRepository).get(1);
  }

  @Test
  public void testArchiveProject() throws Exception {
    String postBodyStr = "{ \"isArchived\": true }";
    mockMvc.perform(post("/projects/1/setArchivedStatus")
            .content(postBodyStr)
            .principal(user)
            .contentType(WebConstants.getApplicationJsonUtf8Value()))
            .andExpect(status().isOk());
    verify(projectService).checkOwnership(1, user);
    verify(projectRepository).setArchived(1, true);
  }

  @Test
  public void testUnArchiveProject() throws Exception {
    String postBodyStr = "{ \"isArchived\": false }";
    mockMvc.perform(post("/projects/1/setArchivedStatus")
            .content(postBodyStr)
            .principal(user)
            .contentType(WebConstants.getApplicationJsonUtf8Value()))
            .andExpect(status().isOk());
    verify(projectService).checkOwnership(1, user);
    verify(projectRepository).setArchived(1, false);
  }
}
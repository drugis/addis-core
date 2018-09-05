package org.drugis.addis.workspaceSettings.controller;

import org.drugis.addis.config.TestConfig;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.security.Account;
import org.drugis.addis.util.WebConstants;
import org.drugis.addis.workspaceSettings.WorkspaceSettings;
import org.drugis.addis.workspaceSettings.repository.WorkspaceSettingsRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@WebAppConfiguration
public class WorkspaceSettingsControllerTest {
  private MockMvc mockMvc;

  @Mock
  private ProjectService projectService;

  @Mock
  private WorkspaceSettingsRepository workspaceSettingsRepository;

  @InjectMocks
  private WorkspaceSettingsController workspaceSettingsController = new WorkspaceSettingsController();

  private Principal user;

  @Before
  public void setUp() {
    initMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(workspaceSettingsController).build();
    user = mock(Principal.class);
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(workspaceSettingsRepository);
    reset(projectService);
  }

  @Test
  public void testGet() throws Exception {
    String settings = "some settings";
    when(workspaceSettingsRepository.get(3)).thenReturn(new WorkspaceSettings(3, settings));
    mockMvc.perform(get("/projects/1/analyses/3/workspaceSettings"))
            .andExpect(status().isOk());
    verify(workspaceSettingsRepository).get(3);
  }

  @Test
  public void testPutWithoutCredentials() throws Exception {
    String body = "some settings";
    doThrow(new MethodNotAllowedException()).when(projectService).checkOwnership(1, user);
    mockMvc.perform(
            put("/projects/1/analyses/3/workspaceSettings")
                    .content(body)
                    .principal(user)
                    .contentType(WebConstants.getApplicationJsonUtf8Value()))
            .andExpect(status().isForbidden());
  }

  @Test
  public void testPut() throws Exception {
    String body = "{settings : {someSetting: true},toggledColumns: {col1: true}}";
    String settingsForRepository = "{settings : {someSetting: true},toggledColumns: {col1: true}}";
    mockMvc.perform(
            put("/projects/1/analyses/3/workspaceSettings")
                    .content(body)
                    .principal(user)
                    .contentType(WebConstants.getApplicationJsonUtf8Value()))
            .andExpect(status().isOk());
    verify(workspaceSettingsRepository).put(3, settingsForRepository);

  }
}

package org.drugis.addis.toggledColumns.controller;

import org.drugis.addis.TestUtils;
import org.drugis.addis.config.TestConfig;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.security.Account;
import org.drugis.addis.toggledColumns.ToggledColumns;
import org.drugis.addis.toggledColumns.repository.ToggledColumnsRepository;
import org.drugis.addis.util.WebConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.web.WebAppConfiguration;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@WebAppConfiguration
public class ToggledColumnsControllerTest {
  private MockMvc mockMvc;

  @Mock
  private ProjectService projectService;

  @Mock
  private ToggledColumnsRepository toggledColumnsRepository;

  @InjectMocks
  private ToggledColumnsController toggledColumnsController = new ToggledColumnsController();

  private Principal user;
  private Account gert = new Account(3, "gert", "Gert", "van Valkenhoef", "gert@test.com");

  @Before
  public void setUp() {
    initMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(toggledColumnsController).build();
    user = mock(Principal.class);
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(toggledColumnsRepository);
    reset(projectService);
  }

  @Test
  public void testGet() throws Exception {
    when(toggledColumnsRepository.get(3)).thenReturn(new ToggledColumns(3, "some toggling"));
    mockMvc.perform(get("/projects/1/analyses/3/toggledColumns"))
            .andExpect(status().isOk());
    verify(toggledColumnsRepository).get(3);
  }

  @Test
  public void testPutWithoutCredentials() throws Exception {
    String body = TestUtils.createJson(new ToggledColumns(1, "criterion: true, description:false, references: true, units: false"));
    doThrow(new MethodNotAllowedException()).when(projectService).checkOwnership(1, user);
    mockMvc.perform(
            put("/projects/1/analyses/3/toggledColumns")
                    .content(body)
                    .principal(user)
                    .contentType(WebConstants.getApplicationJsonUtf8Value()))
            .andExpect(status().isForbidden());
  }

  @Test
  public void testPut() throws Exception {
    String toggledColumns = "{criterion: true, description:false, references: true, units: false}";
    String toggledColumnsForRepository = "{\"analysisId\":1,\"toggledColumns\":{criterion: true, description:false, references: true, units: false}}";
    String body = TestUtils.createJson(new ToggledColumns(1, toggledColumns));
    mockMvc.perform(
            put("/projects/1/analyses/3/toggledColumns")
                    .content(body)
                    .principal(user)
                    .contentType(WebConstants.getApplicationJsonUtf8Value()))
            .andExpect(status().isOk());
    verify(toggledColumnsRepository).put(3, toggledColumnsForRepository);

  }
}

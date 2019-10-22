package org.drugis.addis.subProblems.controller;

import org.drugis.addis.TestUtils;
import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.config.TestConfig;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.security.Account;
import org.drugis.addis.subProblems.SubProblem;
import org.drugis.addis.subProblems.controller.command.SubProblemCommand;
import org.drugis.addis.subProblems.repository.SubProblemRepository;
import org.drugis.addis.subProblems.service.SubProblemService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;
import java.security.Principal;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by joris on 8-5-17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@WebAppConfiguration
public class SubProblemControllerTest {
  private MockMvc mockMvc;

  @Mock
  private SubProblemRepository subProblemRepository;

  @Mock
  private SubProblemService subProblemService;

  @Mock
  private ProjectService projectService;

  @Inject
  private WebApplicationContext webApplicationContext;

  @Mock
  private AnalysisService analysisService;

  @InjectMocks
  private SubProblemController subProblemController = new SubProblemController();

  private Principal user;
  private Account gert = new Account(3, "gert", "Gert", "van Valkenhoef", "gert@test.com");

  @Before
  public void setUp() {
    initMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(subProblemController).build();
    user = mock(Principal.class);
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(subProblemRepository);
    reset(projectService, analysisService);

  }

  @Test
  public void testGet() throws Exception {
    when(subProblemRepository.get(3)).thenReturn(new SubProblem(2, "{}", "Default"));
    mockMvc.perform(get("/projects/1/analyses/2/problems/3"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.title", is("Default")));
    verify(subProblemRepository).get(3);
  }

  @Test
  public void testCreateWithoutCredentialsFails() throws Exception {
    SubProblemCommand subProblemCommand = new SubProblemCommand("{}", "Degauss", "scenarioState");
    String body = TestUtils.createJson(subProblemCommand);
    doThrow(new MethodNotAllowedException()).when(projectService).checkOwnership(1,user);
    mockMvc.perform(
            post("/projects/1/analyses/2/problems")
                    .content(body)
                    .principal(user)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
  }

  @Test
  public void testCreateWithWrongCoordinateFails() throws Exception {
    SubProblemCommand subProblemCommand = new SubProblemCommand("{}", "Degauss", "scenarioState");
    String body = TestUtils.createJson(subProblemCommand);
    doThrow(new ResourceDoesNotExistException()).when(analysisService).checkCoordinates(1,2);
    mockMvc.perform(
            post("/projects/1/analyses/2/problems")
                    .content(body)
                    .principal(user)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
  }

  @Test
  public void testCreate() throws Exception {
    SubProblemCommand subProblemCommand = new SubProblemCommand("{}", "Degauss", "{}");
    String body = TestUtils.createJson(subProblemCommand);

    when(subProblemService.createSubProblem(2, "\"{}\"", "Degauss", "\"{}\"")).thenReturn(new SubProblem(2, "{}", "Degauss"));

    mockMvc.perform(
            post("/projects/1/analyses/2/problems")
                    .content(body)
                    .principal(user)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title", is("Degauss")));
    verify(subProblemService).createSubProblem(2, "\"{}\"", "Degauss", "\"{}\"");
  }


}
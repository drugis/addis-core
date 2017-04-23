package org.drugis.addis.scaledUnits.controller;

import org.drugis.addis.TestUtils;
import org.drugis.addis.config.TestConfig;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.projects.Project;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.projects.repository.ReportRepository;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.scaledUnits.ScaledUnit;
import org.drugis.addis.scaledUnits.ScaledUnitCommand;
import org.drugis.addis.scaledUnits.repository.ScaledUnitRepository;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.util.WebConstants;
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

import javax.inject.Inject;
import java.net.URI;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by joris on 19-4-17. Hell yeah!
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@WebAppConfiguration
public class ScaledUnitControllerTest {
  private MockMvc mockMvc;
  private int projectId = 1;
  private Principal user;
  private Account gert = new Account(1, "gert", "Gert", "van Valkenhoef", "gert@test.com");
  private Project project;
  @Mock
  private ScaledUnitRepository scaledUnitRepository;

  @Mock
  private ProjectService projectService;

  @InjectMocks
  private ScaledUnitController scaledUnitController;


  @Before
  public void setUp() throws ResourceDoesNotExistException {
    initMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(scaledUnitController).build();
    projectService = mock(ProjectService.class);
    user = mock(Principal.class);
    when(user.getName()).thenReturn("gert");
    project = mock(Project.class);
    when(project.getOwner()).thenReturn(gert);
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(scaledUnitRepository);
    verifyNoMoreInteractions(projectService);
  }

  @Test
  public void testQuery() throws Exception {
    ScaledUnit scaledUnit1 = new ScaledUnit(-1, projectId, URI.create("http://gram.com"), 0.1, "decigram");
    ScaledUnit scaledUnit2 = new ScaledUnit(-3, projectId, URI.create("http://gram.com"), 0.01, "centigram");
    List<ScaledUnit> scaledUnits = Arrays.asList(scaledUnit1, scaledUnit2);
    when(scaledUnitRepository.query(projectId)).thenReturn(scaledUnits);

    mockMvc.perform(get("/projects/" + projectId + "/scaledUnits"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].name", is("decigram")));

    verify(scaledUnitRepository).query(projectId);
  }

  @Test
  public void testCreate() throws Exception {
    URI conceptUri = URI.create("http://gram.com");
    double multiplier = 0.001;
    String name = "scaled unit";
    ScaledUnitCommand scaledUnitCommand = new ScaledUnitCommand(conceptUri, multiplier, name);
    String body = TestUtils.createJson(scaledUnitCommand);
    mockMvc.perform(post("/projects/" + projectId + "/scaledUnits")
            .content(body)
            .principal(user)
            .contentType(WebConstants.getApplicationJsonUtf8Value()))
            .andExpect(status().isCreated());
    verify(scaledUnitRepository).create(projectId, conceptUri, multiplier, name);
  }

}
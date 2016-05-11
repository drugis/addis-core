package org.drugis.addis.models.controller;

import org.drugis.addis.config.TestConfig;
import org.drugis.addis.interventions.service.impl.InvalidTypeForDoseCheckException;
import org.drugis.addis.models.exceptions.InvalidModelException;
import org.drugis.addis.patavitask.PataviTaskUriHolder;
import org.drugis.addis.patavitask.controller.PataviTaskController;
import org.drugis.addis.patavitask.service.PataviTaskService;
import org.drugis.addis.trialverse.service.impl.ReadValueException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.net.URI;
import java.security.Principal;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@WebAppConfiguration
public class PataviTaskControllerTest {

  private Principal user;

  private MockMvc mockMvc;

  @Mock
  private PataviTaskService pataviTaskService;

  @InjectMocks
  private PataviTaskController pataviTaskController;

  @Before
  public void setUp() {
    pataviTaskController = new PataviTaskController();
    mockMvc = MockMvcBuilders.standaloneSetup(pataviTaskController).build();
    MockitoAnnotations.initMocks(this);
    user = mock(Principal.class);
    when(user.getName()).thenReturn("gert");
  }

  @After
  public void afterTest() {
    verifyNoMoreInteractions(pataviTaskService);
  }

  @Test
  public void testGet() throws Exception, InvalidModelException, ReadValueException, InvalidTypeForDoseCheckException {
    URI uri = URI.create("www.nogonahappen.com");
    PataviTaskUriHolder pataviTaskUriHolder = new PataviTaskUriHolder(uri);
    int projectId = 45;
    int analysisId = 55;
    int modelId = 37;
    when(pataviTaskService.getPataviTaskUriHolder(projectId, analysisId, modelId)).thenReturn(pataviTaskUriHolder);
    mockMvc.perform(get("/projects/45/analyses/55/models/37/task").principal(user))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.uri", equalTo(uri)))
    ;
    verify(pataviTaskService).getPataviTaskUriHolder(projectId, analysisId, modelId);
  }
}
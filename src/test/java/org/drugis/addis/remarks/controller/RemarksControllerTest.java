package org.drugis.addis.remarks.controller;


import org.drugis.addis.config.TestConfig;
import org.drugis.addis.remarks.Remarks;
import org.drugis.addis.remarks.repository.RemarksRepository;
import org.drugis.addis.util.WebConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.inject.Inject;

import java.security.Principal;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * Created by connor on 17-9-14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@WebAppConfiguration
public class RemarksControllerTest {
  private MockMvc mockMvc;

  @Inject
  RemarksRepository remarksRepository;
  private Principal user;

  @InjectMocks
  private RemarksController remarksController;

  @Before
  public void setUp() {
    reset(remarksRepository);
    remarksController = new RemarksController();
    MockitoAnnotations.initMocks(this);

    mockMvc = MockMvcBuilders.standaloneSetup(remarksController).build();
    user = mock(Principal.class);

  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(remarksRepository);
  }

  @Test
  public void testGetRemarks() throws Exception {
    Integer remarksId = 11;
    Integer scenarioId = 1;
    String remarksStr = "{" +
            "\"HAM-D responders\":\"test content 1\"" +
            "}";
    Remarks remarks = new Remarks(remarksId, scenarioId, remarksStr);
    when(remarksRepository.get(scenarioId)).thenReturn(remarks);
    mockMvc.perform(get("/projects/22/analyses/2/scenarios/" + scenarioId + "/remarks").principal(user))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.APPLICATION_JSON_UTF8))
            .andExpect(jsonPath("$.id", is(remarksId)))
            .andExpect(jsonPath("$.scenarioId", is(scenarioId)));
    verify(remarksRepository).get(scenarioId);
  }
}

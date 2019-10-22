package org.drugis.addis.ordering.controller;

import org.drugis.addis.TestUtils;
import org.drugis.addis.config.TestConfig;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.ordering.Ordering;
import org.drugis.addis.ordering.repository.OrderingRepository;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.security.Account;
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
public class OrderingControllerTest {
  private MockMvc mockMvc;

  @Mock
  private ProjectService projectService;

  @Mock
  private OrderingRepository orderingRepository;

  @InjectMocks
  private OrderingController orderingController = new OrderingController();

  private Principal user;
  private Account gert = new Account(3, "gert", "Gert", "van Valkenhoef", "gert@test.com");

  @Before
  public void setUp() {
    initMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(orderingController).build();
    user = mock(Principal.class);
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(orderingRepository);
    reset(projectService);
  }

  @Test
  public void testGet() throws Exception {
    when(orderingRepository.get(3)).thenReturn(new Ordering(3, "some ordering"));
    mockMvc.perform(get("/projects/1/analyses/3/ordering"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    verify(orderingRepository).get(3);
  }

  @Test
  public void testPutWithoutCredentials() throws Exception {
    String body = TestUtils.createJson(new Ordering(1, "criteria: [], alternatives: []"));
    doThrow(new MethodNotAllowedException()).when(projectService).checkOwnership(1, user);
    mockMvc.perform(
            put("/projects/1/analyses/3/ordering")
                    .content(body)
                    .principal(user)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
  }

  @Test
  public void testPut() throws Exception {
    String ordering = "criteria: [\"crit1\",\"crit2\"], alternatives: [\"alt1\",\"alt2\"]";
    String orderingForRepository = "{\"analysisId\":1,\"ordering\":criteria: [\"crit1\",\"crit2\"], alternatives: [\"alt1\",\"alt2\"]}";
    String body = TestUtils.createJson(new Ordering(1, ordering));
    mockMvc.perform(
            put("/projects/1/analyses/3/ordering")
                    .content(body)
                    .principal(user)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    verify(orderingRepository).put(3, orderingForRepository);

  }
}

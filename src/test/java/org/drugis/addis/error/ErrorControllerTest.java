package org.drugis.addis.error;

import org.drugis.addis.config.TestConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Assert;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@WebAppConfiguration
public class ErrorControllerTest {
  @Autowired
  private WebApplicationContext context;

  private MockMvc mockMvc;

  @Before
  public void setUp() throws Exception {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
  }

  @Test
  public void testGetErrorView() throws Exception {
    mockMvc.perform(get("/error/404"))
      .andExpect(status().isOk())
      .andExpect(view().name("../error/errorPage.html"))
      .andExpect(model().attribute("errorCode", is(404)))
      .andExpect(model().attribute("reasonPhrase", HttpStatus.NOT_FOUND.getReasonPhrase()));

    mockMvc.perform(get("/error/401"))
      .andExpect(status().isOk())
      .andExpect(view().name("../error/errorPage.html"))
      .andExpect(model().attribute("errorCode", is(401)))
      .andExpect(model().attribute("reasonPhrase", HttpStatus.UNAUTHORIZED.getReasonPhrase()));

    mockMvc.perform(get("/error/403"))
      .andExpect(status().isOk())
      .andExpect(view().name("../error/errorPage.html"))
      .andExpect(model().attribute("errorCode", is(403)))
      .andExpect(model().attribute("reasonPhrase", HttpStatus.FORBIDDEN.getReasonPhrase()));

    mockMvc.perform(get("/error/500"))
      .andExpect(status().isOk())
      .andExpect(view().name("../error/errorPage.html"))
      .andExpect(model().attribute("errorCode", is(500)))
      .andExpect(model().attribute("reasonPhrase", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase()));

    mockMvc.perform(get("/error/503"))
      .andExpect(status().isOk())
      .andExpect(view().name("../error/errorPage.html"))
      .andExpect(model().attribute("errorCode", is(503)))
      .andExpect(model().attribute("reasonPhrase", HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase()));
  }
}

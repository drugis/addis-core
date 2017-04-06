package org.drugis.addis.effectsTables;

import org.drugis.addis.config.TestConfig;
import org.drugis.addis.util.WebConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;

import java.util.Arrays;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by joris on 5-4-17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@WebAppConfiguration
public class EffectsTableControllerTest {
  @Inject
  private WebApplicationContext webApplicationContext;

  @Inject
  private EffectsTableRepository effectsTableRepository;

  private MockMvc mockMvc;

  @Before
  public void setup() {
    reset(effectsTableRepository);
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
  }



  @After
  public void tearDown() {
    verifyNoMoreInteractions(effectsTableRepository);
  }

  @Test
  public void testGetEffectsTable() throws Exception {
    Integer analysisId = 2;
    when(effectsTableRepository.getEffectsTableExclusions(analysisId))
            .thenReturn(Arrays.asList(new EffectsTableExclusion(analysisId, 1), new EffectsTableExclusion(analysisId, 2)));
    mockMvc.perform(get("/projects/1/analyses/2/effectsTable"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()))
            .andExpect(jsonPath("$", hasSize(2)));
    verify(effectsTableRepository).getEffectsTableExclusions(analysisId);
  }

}
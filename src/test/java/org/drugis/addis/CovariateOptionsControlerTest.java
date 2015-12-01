package org.drugis.addis;

import org.apache.http.entity.ContentType;
import org.drugis.addis.covariates.CovariateOption;
import org.drugis.addis.covariates.CovariateOptionsController;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by connor on 12/1/15.
 */
@Configuration
@EnableWebMvc
public class CovariateOptionsControlerTest {

  private MockMvc mockMvc;

  @InjectMocks
  private CovariateOptionsController covariateOptionsController;


  @Before
  public void setUp() {
    initMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(covariateOptionsController).build();
  }

  @Test
  public void getCovariateOptions() throws Exception {
    ResultActions result = mockMvc.perform(get("/covariate-options"));
    result.andExpect(status().isOk());
    result.andExpect(content().contentType(ContentType.APPLICATION_JSON.toString()));
    result.andExpect(jsonPath("$", hasSize(4)));
    result.andExpect(jsonPath("$[0].key", is(CovariateOption.ALLOCATION_RANDOMIZED.toString())));
    result.andExpect(jsonPath("$[0].label", is(CovariateOption.ALLOCATION_RANDOMIZED.getLabel())));
    result.andExpect(jsonPath("$[0].type", is(CovariateOption.ALLOCATION_RANDOMIZED.getType().toString())));
  }
}

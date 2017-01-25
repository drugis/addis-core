package org.drugis.addis.statistics.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.drugis.addis.TestUtils;
import org.drugis.addis.config.TestConfig;
import org.drugis.addis.statistics.command.AbstractMeasurementCommand;
import org.drugis.addis.statistics.command.DichotomousMeasurementCommand;
import org.drugis.addis.statistics.command.EstimatesCommand;
import org.drugis.addis.statistics.model.Estimate;
import org.drugis.addis.statistics.model.Estimates;
import org.drugis.addis.statistics.service.StatisticsService;
import org.drugis.addis.util.WebConstants;
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
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by joris on 25-1-17.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@WebAppConfiguration
public class statisticsControllerTest {
  @Inject
  private WebApplicationContext webApplicationContext;
  @Inject
  private StatisticsService statisticsService;

  private MockMvc mockMvc;

  @Before
  public void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
  }

  @Test
  public void getEstimatesTest() throws Exception {
    URI endpointUri = URI.create("http://endpoints.com/1");
    Integer sampleSize1 = 100;
    Integer count1 = 75;
    URI armA1Uri = URI.create("http://arm.uri/1");
    Integer sampleSize2 = 150;
    Integer count2 = 81;
    URI armA2Uri = URI.create("http://arm.uri/2");
    Integer sampleSize3 = 45;
    Integer count3 = 15;
    URI armA3Uri = URI.create("http://arm.uri/3");

    AbstractMeasurementCommand measurement1 = new DichotomousMeasurementCommand(endpointUri, armA1Uri, count1, sampleSize1);
    AbstractMeasurementCommand[] rest = new AbstractMeasurementCommand[2];
    rest[0] = new DichotomousMeasurementCommand(endpointUri, armA2Uri, count2, sampleSize2);
    rest[1] = new DichotomousMeasurementCommand(endpointUri, armA3Uri, count3, sampleSize3);
    List<AbstractMeasurementCommand> measurements = Lists.asList(measurement1, rest);
    EstimatesCommand command = new EstimatesCommand(measurements);
    String body = TestUtils.createJson(command);

    Map<URI, List<Estimate>> estimates = new HashMap<>();
    URI baselineUri = URI.create("http://baselineUri.com");
    Estimates resultEstimates = new Estimates(baselineUri, estimates);
    when(statisticsService.getEstimates(command)).thenReturn(resultEstimates);

//    ObjectMapper mapper = new ObjectMapper();
//    EstimatesCommand deser = mapper.readValue(body, EstimatesCommand.class);
    mockMvc.perform(post("/statistics/estimates").content(body).contentType(WebConstants.getApplicationJsonUtf8Value()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(WebConstants.getApplicationJsonUtf8Value()));
  }
}

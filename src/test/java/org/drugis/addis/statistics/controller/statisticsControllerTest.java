package org.drugis.addis.statistics.controller;

import org.drugis.addis.TestUtils;
import org.drugis.addis.config.TestConfig;
import org.drugis.addis.statistics.command.AbstractMeasurementCommand;
import org.drugis.addis.statistics.command.ContinuousMeasurementCommand;
import org.drugis.addis.statistics.command.DichotomousMeasurementCommand;
import org.drugis.addis.statistics.command.EstimatesCommand;
import org.drugis.addis.statistics.model.Estimate;
import org.drugis.addis.statistics.model.Estimates;
import org.drugis.addis.statistics.service.StatisticsService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    URI endpoint1Uri = URI.create("http://endpoints.com/dichotomous");
    URI endpoint2Uri = URI.create("http://endpoints.com/continuous");
    Integer sampleSize1 = 100;
    Integer count1 = 75;
    URI armA1Uri = URI.create("http://arm.uri/1");
    Integer sampleSize2 = 150;
    Integer count2 = 81;
    URI armA2Uri = URI.create("http://arm.uri/2");
    Map<String, Double> dichoMeas1 = new HashMap<>();
    dichoMeas1.put("count", Double.valueOf(count1));
    dichoMeas1.put("sampleSize", Double.valueOf(sampleSize1));
    Map<String, Double> dichoMeas2 = new HashMap<>();
    dichoMeas2.put("count", Double.valueOf(count2));
    dichoMeas2.put("sampleSize", Double.valueOf(sampleSize2));
    AbstractMeasurementCommand dichotomous1 = new DichotomousMeasurementCommand(endpoint1Uri, armA1Uri, dichoMeas1);
    AbstractMeasurementCommand dichotomous2 = new DichotomousMeasurementCommand(endpoint1Uri, armA2Uri, dichoMeas2);
    Map<String, Double> contMeas1 = new HashMap<>();
    contMeas1.put("standardDeviation", 1.90);
    contMeas1.put("sampleSize", 20.0);
    Map<String, Double> contMeas2 = new HashMap<>();
    contMeas2.put("standardError", 1.90);
    contMeas2.put("sampleSize", 20.0);
    AbstractMeasurementCommand continuous1 = new ContinuousMeasurementCommand(endpoint2Uri, armA1Uri, contMeas1);
    AbstractMeasurementCommand continuous2 = new ContinuousMeasurementCommand(endpoint2Uri, armA1Uri, contMeas2);
    List<AbstractMeasurementCommand> measurements = Arrays.asList(dichotomous1, dichotomous2, continuous1, continuous2);
    EstimatesCommand command = new EstimatesCommand(measurements);
    String body = TestUtils.createJson(command);

    Map<URI, List<Estimate>> estimates = new HashMap<>();
    URI baselineUri = URI.create("http://baselineUri.com");
    Estimates resultEstimates = new Estimates(baselineUri, estimates);
    when(statisticsService.getEstimates(command)).thenReturn(resultEstimates);

//     ObjectMapper mapper = new ObjectMapper(); // for debugging porpoises
//    EstimatesCommand desert = mapper.readValue(body, EstimatesCommand.class);
    mockMvc.perform(post("/statistics/estimates").content(body).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect((jsonPath("$.baselineUri", is(resultEstimates.getBaselineUri().toString()))));
  }
}

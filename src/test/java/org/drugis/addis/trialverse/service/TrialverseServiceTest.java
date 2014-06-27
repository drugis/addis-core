package org.drugis.addis.trialverse.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.drugis.addis.config.TrialverseServiceTestConfig;
import org.drugis.addis.trialverse.model.*;
import org.drugis.addis.trialverse.repository.TrialverseRepository;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Created by connor on 25-3-14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TrialverseServiceTestConfig.class})
public class TrialverseServiceTest {

  @Inject
  TrialverseRepository trialverseRepository;

  @Inject
  TrialverseService trialverseService;


  @After
  public void tearDown() {
    verifyNoMoreInteractions(trialverseRepository);
  }

  @Test
  public void testGetVariablesByOutcomeIds() throws IOException {
    Variable variable1 = new Variable(1L, 11L, "Nausea", "description 1", "my unit is...", true,
            MeasurementType.CONTINUOUS, VariableType.ADVERSE_EVENT);
    Variable variable2 = new Variable(2L, 12L, "HAM-D Responders", "description 2", "my unit is...", true,
            MeasurementType.RATE, VariableType.ENDPOINT);
    List<Variable> variables = Arrays.asList(variable1, variable2);
    List<Long> outcomeIds = Arrays.asList(1L, 2L);
    when(trialverseRepository.getVariablesByOutcomeIds(outcomeIds)).thenReturn(variables);
    ObjectMapper objectMapper = new ObjectMapper();

    // EXECUTOR
    List<ObjectNode> serialisedVars = trialverseService.getVariablesByIds(outcomeIds);

    List<Variable> resultVars = objectMapper.readValue(serialisedVars.toString(), new TypeReference<List<Variable>>() {
    });
    assertEquals(variables, resultVars);
    verify(trialverseRepository).getVariablesByOutcomeIds(outcomeIds);
  }

  @Test
  public void testGetArmNamesByDrugIds() {
    trialverseService.getArmsByDrugIds(anyInt(), anyList());
    verify(trialverseRepository).getArmsByDrugIds(anyInt(), anyList());
  }

  @Test
  public void testGetOrderedMeasurements() throws IOException {
    Integer studyId = 1;
    List<Long> outcomeIds = Arrays.asList(10L, 11L);
    List<Long> armIds = Arrays.asList(20L, 21L);

    Long variableId = 10L;
    Long measurementMomentId = 30L;
    Long armId = 20L;
    MeasurementAttribute attribute = MeasurementAttribute.RATE;
    Measurement measurement = new Measurement(studyId.longValue(), variableId, measurementMomentId, armId, attribute, 60L, null);
    List<Measurement> measurements = Arrays.asList(measurement);
    when(trialverseRepository.getOrderedMeasurements(outcomeIds, armIds)).thenReturn(measurements);

    // execute
    List<ObjectNode> serialisedResult = trialverseService.getOrderedMeasurements(outcomeIds, armIds);

    ObjectMapper mapper = new ObjectMapper();

    ObjectNode node = serialisedResult.get(0);
    assertEquals(measurement.getStudyId(), mapper.convertValue(node.get("studyId"), Long.class));
    assertEquals(measurement.getArmId(), mapper.convertValue(node.get("armId"), Long.class));
    assertEquals(measurement.getVariableId(), mapper.convertValue(node.get("variableId"), Long.class));
    assertEquals(measurement.getMeasurementMomentId(), mapper.convertValue(node.get("measurementMomentId"), Long.class));
    assertEquals(measurement.getMeasurementAttribute(), mapper.convertValue(node.get("measurementAttribute"), MeasurementAttribute.class));
    assertEquals(measurement.getRealValue(), mapper.convertValue(node.get("realValue"), Double.class));
    assertEquals(measurement.getIntegerValue(), mapper.convertValue(node.get("integerValue"), Long.class));
    verify(trialverseRepository).getOrderedMeasurements(outcomeIds, armIds);
  }

}

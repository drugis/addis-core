package org.drugis.addis.trialverse.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.drugis.addis.config.TrialverseServiceTestConfig;
import org.drugis.addis.trialverse.model.Measurement;
import org.drugis.addis.trialverse.model.MeasurementType;
import org.drugis.addis.trialverse.model.Variable;
import org.drugis.addis.trialverse.model.VariableType;
import org.drugis.addis.trialverse.repository.TrialverseRepository;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
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
    Set<String> outcomeIds = new HashSet(Arrays.asList("1L", "2L"));
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
    trialverseService.getArmsByDrugIds(anyString(), anyList());
    verify(trialverseRepository).getArmsByDrugIds(anyString(), anyList());
  }

  @Test
  public void testGetOrderedMeasurements() throws IOException {
    String studyUId = "1";
    List<String> outcomeIds = Arrays.asList("10L", "11L");
    List<String> armUids = Arrays.asList("20L", "21L");

    String variableUid = "10L";
    String armUid = "20L";
    Measurement measurement = new Measurement(studyUId, variableUid, armUid, 60L, 20L, null, null);
    List<Measurement> measurements = Arrays.asList(measurement);
    when(trialverseRepository.getOrderedMeasurements(outcomeIds, armUids)).thenReturn(measurements);

    // execute
    List<ObjectNode> serialisedResult = trialverseService.getOrderedMeasurements(outcomeIds, armUids);

    ObjectMapper mapper = new ObjectMapper();

    ObjectNode node = serialisedResult.get(0);
    assertEquals(measurement.getStudyUid(), mapper.convertValue(node.get("studyUId"), Long.class));
    assertEquals(measurement.getArmUid(), mapper.convertValue(node.get("armUid"), Long.class));
    assertEquals(measurement.getVariableUid(), mapper.convertValue(node.get("variableUid"), Long.class));
    assertEquals(measurement.getSampleSize(), mapper.convertValue(node.get("sampleSize"), Long.class));
    verify(trialverseRepository).getOrderedMeasurements(outcomeIds, armUids);
  }

}

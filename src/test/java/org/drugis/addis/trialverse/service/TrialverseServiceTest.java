package org.drugis.addis.trialverse.service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.drugis.addis.trialverse.model.MeasurementType;
import org.drugis.addis.trialverse.model.Variable;
import org.drugis.addis.trialverse.model.VariableType;
import org.drugis.addis.trialverse.repository.TrialverseRepository;
import org.drugis.addis.trialverse.service.impl.TrialverseServiceImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Created by connor on 25-3-14.
 */
public class TrialverseServiceTest {

  @Mock
  TrialverseRepository trialverseRepository;

  @InjectMocks
  TrialverseService trialverseService;

  @Before
  public void setUp() {
    trialverseService = new TrialverseServiceImpl();
    MockitoAnnotations.initMocks(this);
  }

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
    List<ObjectNode> serialisedVars = trialverseService.getVariablesByOutcomeIds(outcomeIds);

    List<Variable> resultVars = objectMapper.readValue(serialisedVars.toString(), new TypeReference<List<Variable>>() {
    });
    assertEquals(variables, resultVars);
    verify(trialverseRepository).getVariablesByOutcomeIds(outcomeIds);
  }

  @Test
  public void testGetArmNamesByDrugIds() {
    trialverseService.getArmNamesByDrugIds(anyInt(), anyList());
    verify(trialverseRepository).getArmNamesByDrugIds(anyInt(), anyList());
  }

}

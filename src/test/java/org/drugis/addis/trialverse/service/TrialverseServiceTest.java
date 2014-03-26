package org.drugis.addis.trialverse.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.drugis.addis.trialverse.model.Variable;
import org.drugis.addis.trialverse.repository.TrialverseRepository;
import org.drugis.addis.trialverse.service.impl.TrialverseServiceImpl;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
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

  @Test
  public void testJSONVariables() throws IOException {
    Variable variable1 = new Variable(1L, 11L, "variable 1", "description 1", "my unit is...", true, "RATE", "CONTINUOUS");
    Variable variable2 = new Variable(2L, 12L, "variable 2", "description 2", "my unit is...", true, "RATE", "CONTINUOUS");
    List<Variable> variables = Arrays.asList(variable1, variable2);
    List<Long> outcomeIds = Arrays.asList(1L, 2L);
    when(trialverseRepository.getVariablesByOutcomeIds(outcomeIds)).thenReturn(variables);
    ObjectMapper objectMapper = new ObjectMapper();
    List<JSONObject> serialisedVars = trialverseService.getVariablesByOutcomeIds(outcomeIds);
    List<Variable> resultVars = objectMapper.readValue(serialisedVars.toString(), new TypeReference<List<Variable>>() {} );
    assertEquals(variables, resultVars);
  }
}
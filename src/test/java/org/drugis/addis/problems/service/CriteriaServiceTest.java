package org.drugis.addis.problems.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.tuple.Pair;
import org.drugis.addis.analyses.Analysis;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.problems.model.CriterionEntry;
import org.drugis.addis.problems.model.MeasurementType;
import org.drugis.addis.problems.model.PartialValueFunction;
import org.drugis.addis.problems.model.Variable;
import org.drugis.addis.projects.Project;
import org.drugis.addis.trialverse.service.TrialverseService;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.*;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by connor on 1-4-14.
 */
public class CriteriaServiceTest {

  @Mock
  private TriplestoreService triplestoreService;

  @Mock
  private TrialverseService trialverseService;

  @InjectMocks
  private CriteriaService criteriaService;

  ObjectMapper mapper = new ObjectMapper();

  private int studyId;
  private long variableId;
  private String outcomeUri;
  private Map<Long, String> trialverseVariables;
  private Outcome outcome;
  private Project project;
  private Analysis analysis;
  private PartialValueFunction partialValueFunction;
  private Map<String, Outcome> outcomeMap;

  @Before
  public void setUp() {
    criteriaService = new CriteriaService();
    initMocks(this);

    analysis = mock(Analysis.class);
    when(analysis.getStudyId()).thenReturn(studyId);

    project = mock(Project.class);
    int trialverseId = 1;
    when(project.getTrialverseId()).thenReturn(trialverseId);

    outcomeUri = "outcomeUri";
    outcomeMap = new HashMap<>();
    outcome = mock(Outcome.class);
    when(outcome.getName()).thenReturn("outcomeName");
    when(outcome.getSemanticOutcomeUri()).thenReturn(outcomeUri);
    outcomeMap.put(outcome.getSemanticOutcomeUri(), outcome);

    when(analysis.getSelectedOutcomes()).thenReturn(Arrays.asList(outcome));
    studyId = 11;
    variableId = 22;
    trialverseVariables = new HashMap<>();
    partialValueFunction = null;
    when(triplestoreService.getTrialverseVariables(project.getTrialverseId(), analysis.getStudyId(), outcomeMap.keySet())).thenReturn(trialverseVariables);

  }


  @After
  public void cleanUp() {
    verify(trialverseService).getVariablesByIds(trialverseVariables.keySet());
    verify(triplestoreService).getTrialverseVariables(project.getTrialverseId(), analysis.getStudyId(), outcomeMap.keySet());
    verifyNoMoreInteractions(triplestoreService, trialverseService);
  }


  @Test
  public void testCreateVariableCriteriaPairsForARateVariable() {
    Variable variable = new Variable(variableId, new Long(studyId), "variableName", "desc", "unit", true, MeasurementType.RATE, "varType");
    ObjectNode variableJSONNode = mapper.convertValue(variable, ObjectNode.class);
    trialverseVariables.put(variable.getId(), outcomeUri);
    when(trialverseService.getVariablesByIds(trialverseVariables.keySet())).thenReturn(Arrays.asList(variableJSONNode));

    List<Pair<Variable, CriterionEntry>> result = criteriaService.createVariableCriteriaPairs(project, analysis);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(variable, result.get(0).getLeft());
    List<Double> rateScale = Arrays.asList(0.0, 1.0);
    CriterionEntry expectedCriterionEntry = new CriterionEntry(outcome.getName(), rateScale, partialValueFunction);
    assertEquals(expectedCriterionEntry, result.get(0).getRight());

  }

  @Test
  public void testCreateVariableCriteriaPairsForAContinousVariable() {
    Variable variable = new Variable(variableId, new Long(studyId), "variableName", "desc", "unit", true, MeasurementType.CONTINUOUS, "varType");
    ObjectNode variableJSONNode = mapper.convertValue(variable, ObjectNode.class);
    trialverseVariables.put(variable.getId(), outcomeUri);
    when(trialverseService.getVariablesByIds(trialverseVariables.keySet())).thenReturn(Arrays.asList(variableJSONNode));

    List<Pair<Variable, CriterionEntry>> result = criteriaService.createVariableCriteriaPairs(project, analysis);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(variable, result.get(0).getLeft());
    List<Double> continousScale = Arrays.asList(null, null);
    CriterionEntry expectedCriterionEntry = new CriterionEntry(outcome.getName(), continousScale, partialValueFunction);
    assertEquals(expectedCriterionEntry, result.get(0).getRight());

  }

  @Test(expected = EnumConstantNotPresentException.class)
  public void testCreateVariableCriteriaPairsForACategoricalVariable() {
    Variable variable = new Variable(variableId, new Long(studyId), "variableName", "desc", "unit", true, MeasurementType.CATEGORICAL, "varType");
    ObjectNode variableJSONNode = mapper.convertValue(variable, ObjectNode.class);
    trialverseVariables.put(variable.getId(), outcomeUri);
    when(trialverseService.getVariablesByIds(trialverseVariables.keySet())).thenReturn(Arrays.asList(variableJSONNode));

    criteriaService.createVariableCriteriaPairs(project, analysis);
  }


}

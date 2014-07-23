package org.drugis.addis.problems.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.tuple.Pair;
import org.drugis.addis.analyses.SingleStudyBenefitRiskAnalysis;
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

import static org.junit.Assert.assertEquals;
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

  private String studyUid;
  private String variableUid;
  private String outcomeUid;
  private Map<String, String> trialverseVariables;
  private Outcome outcome;
  private Project project;
  private SingleStudyBenefitRiskAnalysis analysis;
  private PartialValueFunction partialValueFunction;
  private Map<String, Outcome> outcomeMap;

  @Before
  public void setUp() {
    criteriaService = new CriteriaService();
    initMocks(this);

    analysis = mock(SingleStudyBenefitRiskAnalysis.class);
    when(analysis.getStudyUid()).thenReturn(studyUid);

    project = mock(Project.class);
    String trialverseUid = "abc";
    when(project.getNamespaceUid()).thenReturn(trialverseUid);

    outcomeUid = "outcomeUri";
    outcomeMap = new HashMap<>();
    outcome = mock(Outcome.class);
    when(outcome.getName()).thenReturn("outcomeName");
    when(outcome.getSemanticOutcomeUri()).thenReturn(outcomeUid);
    outcomeMap.put(outcome.getSemanticOutcomeUri(), outcome);

    when(analysis.getSelectedOutcomes()).thenReturn(Arrays.asList(outcome));
    studyUid = "studyUid";
    variableUid = "varUid";
    trialverseVariables = new HashMap<>();
    partialValueFunction = null;
    when(triplestoreService.getTrialverseVariables(project.getNamespaceUid(), analysis.getStudyUid(), outcomeMap.keySet())).thenReturn(trialverseVariables);

  }


  @After
  public void cleanUp() {
    verify(trialverseService).getVariablesByIds(trialverseVariables.keySet());
    verify(triplestoreService).getTrialverseVariables(project.getNamespaceUid(), analysis.getStudyUid(), outcomeMap.keySet());
    verifyNoMoreInteractions(triplestoreService, trialverseService);
  }


  @Test
  public void testCreateVariableCriteriaPairsForARateVariable() {
    Variable variable = new Variable(variableUid, studyUid, "variableName", "desc", "unit", true, MeasurementType.RATE, "varType");
    ObjectNode variableJSONNode = mapper.convertValue(variable, ObjectNode.class);
    trialverseVariables.put(variable.getUid(), outcomeUid);
    when(trialverseService.getVariablesByIds(trialverseVariables.keySet())).thenReturn(Arrays.asList(variableJSONNode));

    List<Pair<Variable, CriterionEntry>> result = criteriaService.createVariableCriteriaPairs(project, analysis);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(variable, result.get(0).getLeft());
    List<Double> rateScale = Arrays.asList(0.0, 1.0);
    CriterionEntry expectedCriterionEntry = new CriterionEntry(outcome.getSemanticOutcomeUri(), outcome.getName(), rateScale, partialValueFunction);
    assertEquals(expectedCriterionEntry, result.get(0).getRight());

  }

  @Test
  public void testCreateVariableCriteriaPairsForAContinousVariable() {
    Variable variable = new Variable(variableUid, studyUid, "variableName", "desc", "unit", true, MeasurementType.CONTINUOUS, "varType");
    ObjectNode variableJSONNode = mapper.convertValue(variable, ObjectNode.class);
    trialverseVariables.put(variable.getUid(), outcomeUid);
    when(trialverseService.getVariablesByIds(trialverseVariables.keySet())).thenReturn(Arrays.asList(variableJSONNode));

    List<Pair<Variable, CriterionEntry>> result = criteriaService.createVariableCriteriaPairs(project, analysis);

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(variable, result.get(0).getLeft());
    List<Double> continousScale = Arrays.asList(null, null);
    CriterionEntry expectedCriterionEntry = new CriterionEntry(outcome.getSemanticOutcomeUri(), outcome.getName(), continousScale, partialValueFunction);
    assertEquals(expectedCriterionEntry, result.get(0).getRight());

  }

  @Test(expected = EnumConstantNotPresentException.class)
  public void testCreateVariableCriteriaPairsForACategoricalVariable() {
    Variable variable = new Variable(variableUid, studyUid, "variableName", "desc", "unit", true, MeasurementType.CATEGORICAL, "varType");
    ObjectNode variableJSONNode = mapper.convertValue(variable, ObjectNode.class);
    trialverseVariables.put(variable.getUid(), outcomeUid);
    when(trialverseService.getVariablesByIds(trialverseVariables.keySet())).thenReturn(Arrays.asList(variableJSONNode));

    criteriaService.createVariableCriteriaPairs(project, analysis);
  }


}

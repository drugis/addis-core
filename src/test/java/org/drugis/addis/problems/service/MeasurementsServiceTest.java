package org.drugis.addis.problems.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.drugis.addis.analyses.SingleStudyBenefitRiskAnalysis;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.problems.model.AlternativeEntry;
import org.drugis.addis.problems.model.Measurement;
import org.drugis.addis.problems.service.impl.PerformanceTableBuilder;
import org.drugis.addis.projects.Project;
import org.drugis.addis.trialverse.service.TrialverseService;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by daan on 1-4-14.
 */
public class MeasurementsServiceTest {

  @Mock
  private PerformanceTableBuilder performanceTableBuilder;

  @Mock
  private TriplestoreService triplestoreService;

  @Mock
  private TrialverseService trialverseService;

  @InjectMocks
  private MeasurementsService measurementsService;

  private ObjectMapper mapper = new ObjectMapper();

  @Before
  public void setUp() throws Exception {
    measurementsService = new MeasurementsService();
    initMocks(this);
  }

  @Test
  public void testCreatePerformanceTable() throws Exception {
    String trialverseUid = "abc";
    String studyUid = "studyUid";
    String outcomeUri = "outcomeUri";
    String outcomeUid = "outcomeUid";
    String armUid = "armuid";

    Project project = mock(Project.class);
    when(project.getNamespaceUid()).thenReturn(trialverseUid);

    SingleStudyBenefitRiskAnalysis analysis = mock(SingleStudyBenefitRiskAnalysis.class);
    when(analysis.getStudyUid()).thenReturn(studyUid);

    Outcome outcome = mock(Outcome.class);
    when(outcome.getSemanticOutcomeUri()).thenReturn(outcomeUri);

    List<Outcome> outcomes = Arrays.asList(outcome);
    when(analysis.getSelectedOutcomes()).thenReturn(outcomes);

    Map<String, Outcome> outcomeMap = new HashMap<>();
    outcomeMap.put(outcomeUri, outcome);

    Map<String, AlternativeEntry> alternativesCache = new HashMap<>();

    String alternativeUid = "altuid";
    alternativesCache.put(alternativeUid, mock(AlternativeEntry.class));

    Map<String, String> trialverseVariables = new HashMap<>();
    trialverseVariables.put(outcomeUid, "variable name");
    when(triplestoreService.getTrialverseVariables(project.getNamespaceUid(), analysis.getStudyUid(), outcomeMap.keySet())).thenReturn(trialverseVariables);

    Measurement measurement = new Measurement(studyUid, outcomeUid, armUid, 3L, 2L, null, null);
    ObjectNode jsonNode = mapper.convertValue(measurement, ObjectNode.class);
    List<ObjectNode> jsonMeasurements = Arrays.asList(jsonNode);
    when(trialverseService.getOrderedMeasurements(new ArrayList<>(trialverseVariables.keySet()), new ArrayList<>(alternativesCache.keySet()))).thenReturn(jsonMeasurements);

    List<Measurement> measurements = measurementsService.createMeasurements(project, analysis, alternativesCache);

    List<Measurement> expected = Arrays.asList(measurement);

    assertEquals(expected, measurements);

    verify(triplestoreService).getTrialverseVariables(project.getNamespaceUid(), analysis.getStudyUid(), outcomeMap.keySet());
    verify(triplestoreService).getTrialverseVariables(project.getNamespaceUid(), analysis.getStudyUid(), outcomeMap.keySet());
  }
}

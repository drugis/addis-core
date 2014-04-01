package org.drugis.addis.problems.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.drugis.addis.analyses.Analysis;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.problems.model.AlternativeEntry;
import org.drugis.addis.problems.model.Measurement;
import org.drugis.addis.problems.model.MeasurementAttribute;
import org.drugis.addis.problems.service.impl.PerformanceTableBuilder;
import org.drugis.addis.projects.Project;
import org.drugis.addis.trialverse.service.TrialverseService;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
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
    int trialverseId = 1;
    int studyId = 2;
    String outcomeUri = "uri";
    Long outcomeId = 11L;
    long alternativeId = 21L;
    Long measurementMomentId = 31L;
    Long integerValue = 101L;

    Project project = mock(Project.class);
    when(project.getTrialverseId()).thenReturn(trialverseId);

    Analysis analysis = mock(Analysis.class);
    when(analysis.getStudyId()).thenReturn(studyId);

    Outcome outcome = mock(Outcome.class);
    when(outcome.getSemanticOutcomeUri()).thenReturn(outcomeUri);

    List<Outcome> outcomes = Arrays.asList(outcome);
    when(analysis.getSelectedOutcomes()).thenReturn(outcomes);

    Map<String, Outcome> outcomeMap = new HashMap<>();
    outcomeMap.put(outcomeUri, outcome);

    Map<Long, AlternativeEntry> alternativesCache = new HashMap<>();

    alternativesCache.put(alternativeId, mock(AlternativeEntry.class));

    Map<Long, String> trialverseVariables = new HashMap<>();
    trialverseVariables.put(outcomeId, "variable name");
    when(triplestoreService.getTrialverseVariables(project.getTrialverseId(), analysis.getStudyId(), outcomeMap.keySet())).thenReturn(trialverseVariables);

    Measurement measurement = new Measurement((long) studyId, outcomeId, measurementMomentId, alternativeId, MeasurementAttribute.RATE, integerValue, null);
    ObjectNode jsonNode = mapper.convertValue(measurement, ObjectNode.class);
    List<ObjectNode> jsonMeasurements = Arrays.asList(jsonNode);
    when(trialverseService.getOrderedMeasurements(studyId, trialverseVariables.keySet(), alternativesCache.keySet())).thenReturn(jsonMeasurements);

    List<Measurement> measurements = measurementsService.createMeasurements(project, analysis, alternativesCache);

    List<Measurement> expected = Arrays.asList(measurement);

    assertEquals(expected, measurements);
  }
}

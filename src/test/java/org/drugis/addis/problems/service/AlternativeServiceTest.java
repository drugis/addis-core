package org.drugis.addis.problems.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.drugis.addis.analyses.Analysis;
import org.drugis.addis.interventions.Intervention;
import org.drugis.addis.problems.model.AlternativeEntry;
import org.drugis.addis.problems.model.Arm;
import org.drugis.addis.projects.Project;
import org.drugis.addis.trialverse.service.TrialverseService;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Created by connor on 1-4-14.
 */
public class AlternativeServiceTest {

  @Mock
  private TriplestoreService triplestoreService;

  @Mock
  private TrialverseService trialverseService;

  @InjectMocks
  private AlternativeService alternativeService;

  private ObjectMapper mapper;

  private long drugId;
  private String interventionName;
  private Map<String, Intervention> interventionMap;
  private Map<Long, String> drugs;

  private Project project;
  private Analysis analysis;


  @Before
  public void setUp() throws Exception {
    mapper = new ObjectMapper();
    alternativeService = new AlternativeService();
    MockitoAnnotations.initMocks(this);

    int trialverseId = 1;
    int studyId = 101;
    drugId = 2L;
    interventionName = "intervention name";
    String interventionUri = "intervention uri";
    interventionMap = new HashMap<>();
    drugs = new HashMap<>();
    drugs.put(drugId, interventionUri);

    Intervention intervention = mock(Intervention.class);

    List<Intervention> interventions = Arrays.asList(intervention);

    project = mock(Project.class);
    analysis = mock(Analysis.class);

    when(project.getTrialverseId()).thenReturn(trialverseId);
    when(analysis.getStudyId()).thenReturn(studyId);
    when(intervention.getSemanticInterventionUri()).thenReturn(interventionUri);
    when(intervention.getName()).thenReturn(interventionName);
    when(analysis.getSelectedInterventions()).thenReturn(interventions);

    interventionMap.put(intervention.getSemanticInterventionUri(), intervention);

  }

  @Test
  public void testCreateAlternatives() throws Exception {
    List<ObjectNode> jsonArms = Arrays.asList(mapper.convertValue(new Arm(1L, drugId, "armName"), ObjectNode.class));
    when(triplestoreService.getTrialverseDrugs(project.getTrialverseId(), analysis.getStudyId(), interventionMap.keySet())).
            thenReturn(drugs);
    when(trialverseService.getArmsByDrugIds(analysis.getStudyId(), drugs.keySet())).thenReturn(jsonArms);


    Map<Long, AlternativeEntry> alternatives = alternativeService.createAlternatives(project, analysis);

    verify(triplestoreService).getTrialverseDrugs(project.getTrialverseId(), analysis.getStudyId(), interventionMap.keySet());
    verify(trialverseService).getArmsByDrugIds(analysis.getStudyId(), drugs.keySet());
    verifyNoMoreInteractions(triplestoreService, trialverseService);

    AlternativeEntry alternativeEntry = new AlternativeEntry(interventionName);
    assertEquals(alternativeEntry, alternatives.get(1L));
    assertEquals(1, alternatives.size());


  }

}

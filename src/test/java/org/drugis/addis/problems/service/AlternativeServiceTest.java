package org.drugis.addis.problems.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.drugis.addis.analyses.SingleStudyBenefitRiskAnalysis;
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

  private String drugUid;
  private String interventionName;
  private String interventionUri;
  private Map<String, Intervention> interventionMap;
  private Map<String, String> drugs;

  private Project project;
  private SingleStudyBenefitRiskAnalysis analysis;


  @Before
  public void setUp() throws Exception {
    mapper = new ObjectMapper();
    alternativeService = new AlternativeService();
    MockitoAnnotations.initMocks(this);

    String trialverseId = "abc";
    String studyUid = "asd";
    drugUid = "drug";
    interventionName = "intervention name";
    interventionUri = "intervention uri";
    interventionMap = new HashMap<>();
    drugs = new HashMap<>();
    drugs.put(drugUid, interventionUri);

    Intervention intervention = mock(Intervention.class);

    List<Intervention> interventions = Arrays.asList(intervention);

    project = mock(Project.class);
    analysis = mock(SingleStudyBenefitRiskAnalysis.class);

    when(project.getNamespaceUid()).thenReturn(trialverseId);
    when(analysis.getStudyUid()).thenReturn(studyUid);
    when(intervention.getSemanticInterventionUri()).thenReturn(interventionUri);
    when(intervention.getName()).thenReturn(interventionName);
    when(analysis.getSelectedInterventions()).thenReturn(interventions);

    interventionMap.put(intervention.getSemanticInterventionUri(), intervention);

  }

  @Test
  public void testCreateAlternatives() throws Exception {
    List<ObjectNode> jsonArms = Arrays.asList(mapper.convertValue(new Arm("armUid", drugUid, "armName"), ObjectNode.class));
    when(triplestoreService.getTrialverseDrugs(project.getNamespaceUid(), analysis.getStudyUid(), interventionMap.keySet())).
            thenReturn(drugs);
    when(trialverseService.getArmsByDrugIds(analysis.getStudyUid(), drugs.keySet())).thenReturn(jsonArms);


    Map<String, AlternativeEntry> alternatives = alternativeService.createAlternatives(project, analysis);

    verify(triplestoreService).getTrialverseDrugs(project.getNamespaceUid(), analysis.getStudyUid(), interventionMap.keySet());
    verify(trialverseService).getArmsByDrugIds(analysis.getStudyUid(), drugs.keySet());
    verifyNoMoreInteractions(triplestoreService, trialverseService);

    AlternativeEntry alternativeEntry = new AlternativeEntry(interventionUri, interventionName);
    assertEquals(alternativeEntry, alternatives.get(1L));
    assertEquals(1, alternatives.size());


  }

}

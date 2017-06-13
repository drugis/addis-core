package org.drugis.addis.outcomes.service;

import org.drugis.addis.analyses.model.AbstractAnalysis;
import org.drugis.addis.analyses.model.BenefitRiskAnalysis;
import org.drugis.addis.analyses.model.BenefitRiskNMAOutcomeInclusion;
import org.drugis.addis.analyses.model.NetworkMetaAnalysis;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.outcomes.repository.OutcomeRepository;
import org.drugis.addis.outcomes.service.impl.OutcomeServiceImpl;
import org.drugis.addis.trialverse.model.SemanticVariable;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.social.OperationNotPermittedException;

import java.net.URI;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by connor on 10-6-16.
 */
public class OutcomeServiceTest {

  @Mock
  private OutcomeRepository outcomeRepository;

  @Mock
  private AnalysisRepository analysisRepository;

  @InjectMocks
  private OutcomeService outcomeService;

  private Integer outcomeId = 2;
  private Integer projectId = 1;


  @Before
  public void setUp() {
    outcomeService = new OutcomeServiceImpl();
    initMocks(this);
  }

  @Test
  public void editOutcome() throws Exception {
    String name = "name";
    String motivation = "motivation";
    Integer direction = 1;
    Outcome oldIntervention = new Outcome(outcomeId, projectId, "oldName", direction, "oldMotivation", new SemanticVariable(URI.create("uri"), "uriLabel"));
    when(outcomeRepository.get(projectId, outcomeId)).thenReturn(oldIntervention);
    when(outcomeRepository.isExistingOutcomeName(outcomeId, "name")).thenReturn(false);
    Outcome abstractIntervention = outcomeService.updateOutcome(projectId, outcomeId, name, motivation, direction);
    assertEquals(outcomeId, abstractIntervention.getId());
    assertEquals(name, abstractIntervention.getName());
    assertEquals(motivation, abstractIntervention.getMotivation());
  }

  @Test(expected = Exception.class)
  public void editNameAndMotivationCheckDuplicateName() throws Exception {
    String name = "name";
    String motivation = "motivation";
    Integer direction = 1;
    when(outcomeRepository.isExistingOutcomeName(outcomeId, "name")).thenReturn(true);
    outcomeService.updateOutcome(projectId, outcomeId, name, motivation, direction);
  }

  @Test(expected = Exception.class)
  public void editNameAndMotivationOtherProject() throws Exception {
    String name = "name";
    String motivation = "motivation";
    Integer direction = 1;
    when(outcomeRepository.isExistingOutcomeName(outcomeId, "name")).thenReturn(false);
    when(outcomeRepository.get(projectId, outcomeId)).thenReturn(null);
    outcomeService.updateOutcome(projectId, outcomeId, name, motivation, direction);
  }

  @Test
  public void deleteUnused() throws Exception {
    Integer projectId = 37;
    Integer analysisId1 = 42;
    Integer analysisId2 = 13;
    Integer someOutcomeId = -2;
    Outcome someOutcome = new Outcome(someOutcomeId, projectId, "ham", -1, "", new SemanticVariable(URI.create(""), ""));
    AbstractAnalysis analysisWithDifferentOutcome = new NetworkMetaAnalysis(analysisId1, projectId, "analysisWithDifferentOutcome", someOutcome);
    AbstractAnalysis analysisWithoutOutcome = new NetworkMetaAnalysis(analysisId2, projectId, "analysisWithoutOutcome");
    when(analysisRepository.query(projectId)).thenReturn(Arrays.asList(analysisWithDifferentOutcome, analysisWithoutOutcome));

    outcomeService.delete(projectId, outcomeId);

    verify(analysisRepository).query(projectId);
  }

  @Test(expected = OperationNotPermittedException.class)
  public void deleteUsedInNMAFails() throws Exception {
    Integer projectId = 37;
    Integer analysisId1 = 42;
    Integer analysisId2 = 13;
    Integer someOutcomeId = -2;
    Outcome someOutcome = new Outcome(someOutcomeId, projectId, "ham", -1, "", new SemanticVariable(URI.create(""), ""));
    Outcome usedOutcome = new Outcome(outcomeId, projectId, "ham", -1, "", new SemanticVariable(URI.create(""), ""));
    AbstractAnalysis analysisWithDifferentOutcome = new NetworkMetaAnalysis(analysisId1, projectId, "analysisWithDifferentOutcome", someOutcome);
    AbstractAnalysis analysisWithOutcome = new NetworkMetaAnalysis(analysisId2, projectId, "analysisWithoutOutcome", usedOutcome);
    when(analysisRepository.query(projectId)).thenReturn(Arrays.asList(analysisWithDifferentOutcome, analysisWithOutcome));

    outcomeService.delete(projectId, outcomeId);
  }

  @Test(expected = OperationNotPermittedException.class)
  public void deleteUsedInMetaBRFails() throws Exception {
    Integer projectId = 37;
    Integer analysisId1 = 42;
    Integer analysisId2 = 13;
    Integer someOutcomeId = -2;
    BenefitRiskNMAOutcomeInclusion someOutcomeInclusion = new BenefitRiskNMAOutcomeInclusion(analysisId1, someOutcomeId, -3, 5);
    BenefitRiskNMAOutcomeInclusion usedOutcomeInclusion = new BenefitRiskNMAOutcomeInclusion(analysisId1, outcomeId, -3, 5);

    BenefitRiskAnalysis analysisWithDifferentOutcome = new BenefitRiskAnalysis(analysisId1, projectId,
            "analysisWithDifferentOutcome");
    analysisWithDifferentOutcome.setBenefitRiskNMAOutcomeInclusions(Collections.singletonList(someOutcomeInclusion));

    BenefitRiskAnalysis analysisWithOutcome = new BenefitRiskAnalysis(analysisId2, projectId,
            "analysisWithoutOutcome");
    analysisWithOutcome.setBenefitRiskNMAOutcomeInclusions(Arrays.asList(someOutcomeInclusion, usedOutcomeInclusion));

    when(analysisRepository.query(projectId)).thenReturn(Arrays.asList(analysisWithDifferentOutcome, analysisWithOutcome));

    outcomeService.delete(projectId, outcomeId);
  }

}

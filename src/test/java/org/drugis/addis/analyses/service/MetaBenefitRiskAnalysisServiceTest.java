package org.drugis.addis.analyses.service;

import com.google.common.collect.Sets;
import org.drugis.addis.analyses.InterventionInclusion;
import org.drugis.addis.analyses.MbrOutcomeInclusion;
import org.drugis.addis.analyses.MetaBenefitRiskAnalysis;
import org.drugis.addis.analyses.controller.AnalysisUpdateCommand;
import org.drugis.addis.analyses.repository.MetaBenefitRiskAnalysisRepository;
import org.drugis.addis.analyses.service.impl.MetaBenefitRiskAnalysisServiceImpl;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ProblemCreationException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.interventions.model.SimpleIntervention;
import org.drugis.addis.interventions.repository.InterventionRepository;
import org.drugis.addis.interventions.service.impl.InvalidTypeForDoseCheckException;
import org.drugis.addis.patavitask.repository.UnexpectedNumberOfResultsException;
import org.drugis.addis.problems.service.ProblemService;
import org.drugis.addis.scenarios.Scenario;
import org.drugis.addis.scenarios.repository.ScenarioRepository;
import org.drugis.addis.security.Account;
import org.drugis.addis.trialverse.service.impl.ReadValueException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by connor on 9-3-16.
 */
public class MetaBenefitRiskAnalysisServiceTest {

  @Mock
  MetaBenefitRiskAnalysisRepository metaBenefitRiskAnalysisRepository;

  @Mock
  ScenarioRepository scenarioRepository;

  @Mock
  ProblemService problemService;

  @Mock
  InterventionRepository interventionRepository;

  @InjectMocks
  MetaBenefitRiskAnalysisService metaBenefitRiskAnalysisService = new MetaBenefitRiskAnalysisServiceImpl();
  private final Account user = new Account("jondoe", "jon", "doe", "e@mail.com");
  private final Integer projectId = 1;
  private final Integer analysisId = 2;

  @Before
  public void setUp() {
    initMocks(this);
  }


  @Test
  public void testUpdateWithFinalizingAnalysis() throws ResourceDoesNotExistException, SQLException, MethodNotAllowedException, IOException, URISyntaxException, ReadValueException, InvalidTypeForDoseCheckException, UnexpectedNumberOfResultsException, ProblemCreationException {
    MetaBenefitRiskAnalysis analysis = new MetaBenefitRiskAnalysis(analysisId, projectId, "tittle");
    analysis.setFinalized(true);

    MetaBenefitRiskAnalysis oldAnalysis = new MetaBenefitRiskAnalysis(analysisId, projectId, "tittle");

    when(metaBenefitRiskAnalysisRepository.find(analysis.getId())).thenReturn(oldAnalysis);
    when(problemService.getProblem (projectId, analysisId)).thenReturn(null);

    metaBenefitRiskAnalysisService.update(user, projectId, analysis, "");

    verify(metaBenefitRiskAnalysisRepository).find(analysisId);
    verify(problemService).getProblem(projectId, analysisId);
    verify(scenarioRepository).create(analysisId, Scenario.DEFAULT_TITLE, "{\"problem\":}");
    verify(metaBenefitRiskAnalysisRepository).update(user, analysis);
    verifyNoMoreInteractions(metaBenefitRiskAnalysisRepository, scenarioRepository, problemService);
  }

  @Test
  public void testUpdateWithNonFinalizingAnalysis() throws ResourceDoesNotExistException, SQLException, MethodNotAllowedException, IOException, URISyntaxException, ReadValueException, InvalidTypeForDoseCheckException, UnexpectedNumberOfResultsException, ProblemCreationException {
    MetaBenefitRiskAnalysis analysis = new MetaBenefitRiskAnalysis(analysisId, projectId, "tittle");
    MetaBenefitRiskAnalysis oldAnalysis = new MetaBenefitRiskAnalysis(analysisId, projectId, "tittle");

    when(metaBenefitRiskAnalysisRepository.find(analysis.getId())).thenReturn(oldAnalysis);

    metaBenefitRiskAnalysisService.update(user, projectId, analysis,"");

    verify(metaBenefitRiskAnalysisRepository).find(analysisId);
    verify(metaBenefitRiskAnalysisRepository).update(user, analysis);
    verifyNoMoreInteractions(metaBenefitRiskAnalysisRepository, scenarioRepository, problemService);
  }

  @Test(expected = MethodNotAllowedException.class)
  public void testUpdateFinalizedAnalysisFails() throws ResourceDoesNotExistException, SQLException, MethodNotAllowedException, IOException, URISyntaxException, ReadValueException, InvalidTypeForDoseCheckException, UnexpectedNumberOfResultsException, ProblemCreationException {
    MetaBenefitRiskAnalysis analysis = new MetaBenefitRiskAnalysis(analysisId, projectId, "tittle");
    MetaBenefitRiskAnalysis oldAnalysis = new MetaBenefitRiskAnalysis(analysisId, projectId, "tittle");
    oldAnalysis.setFinalized(true);

    when(metaBenefitRiskAnalysisRepository.find(analysis.getId())).thenReturn(oldAnalysis);

    metaBenefitRiskAnalysisService.update(user, projectId, analysis, "");
  }

  @Test
  public void testCleanInclusions() {
    Integer outcomeId = -10;
    Integer nmaId = -100;
    Integer modelId = -1000;
    MetaBenefitRiskAnalysis analysis = new MetaBenefitRiskAnalysis(projectId, "title");
    MetaBenefitRiskAnalysis oldAnalysis = new MetaBenefitRiskAnalysis(projectId, "title");

    Integer sertraId = -1;
    String sertraName = "sertraline";
    URI sertraUri = URI.create("http://sertraUri.com");
    AbstractIntervention sertra = new SimpleIntervention(sertraId, projectId, sertraName, null, sertraUri, "sertraline");
    Integer fluoxId = -2;
    String fluoxName = "fluoxetine";
    URI fluoxUri = URI.create("http://fluoxUri.com");
    AbstractIntervention fluox = new SimpleIntervention(fluoxId, projectId, fluoxName, null, fluoxUri, "Fluoxetine");

    InterventionInclusion sertraInclusion = new InterventionInclusion(analysisId, sertraId);
    InterventionInclusion fluoxInclusion = new InterventionInclusion(analysisId, fluoxId);

    analysis.updateIncludedInterventions(Sets.newHashSet(sertraInclusion));
    HashSet<InterventionInclusion> includedInterventions = Sets.newHashSet(fluoxInclusion, sertraInclusion);
    oldAnalysis.updateIncludedInterventions(includedInterventions);

    MbrOutcomeInclusion outcomeInclusion = new MbrOutcomeInclusion(analysisId, outcomeId, nmaId, modelId);
    outcomeInclusion.setBaseline("{\"name\": \"" + fluoxName + "\"}");
    analysis.setMbrOutcomeInclusions(Collections.singletonList(outcomeInclusion));
    oldAnalysis.setMbrOutcomeInclusions(Collections.singletonList(outcomeInclusion));

    when(interventionRepository.getByProjectIdAndName(projectId, fluoxName)).thenReturn(fluox);
    when(interventionRepository.getByProjectIdAndName(projectId, sertraName)).thenReturn(sertra);

    List<MbrOutcomeInclusion> cleanInclusions = metaBenefitRiskAnalysisService.cleanInclusions(analysis, oldAnalysis);

    assertEquals(Collections.singletonList(new MbrOutcomeInclusion(analysisId, outcomeId, nmaId, modelId)), cleanInclusions);
  }
}
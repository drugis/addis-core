package org.drugis.addis.analyses.service;

import org.drugis.addis.analyses.MetaBenefitRiskAnalysis;
import org.drugis.addis.analyses.repository.MetaBenefitRiskAnalysisRepository;
import org.drugis.addis.analyses.service.impl.MetaBenefitRiskAnalysisServiceImpl;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
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
import java.net.URISyntaxException;
import java.sql.SQLException;

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

  @InjectMocks
  MetaBenefitRiskAnalysisService metaBenefitRiskAnalysisService = new MetaBenefitRiskAnalysisServiceImpl();
  private final Account user = new Account("jondoe", "jon", "doe", "e@mail.com");
  private final Integer projectId = 1;
  private final Integer analysisId = 2;

  @Before
  public void setUp(){
    initMocks(this);
  }


  @Test
  public void testUpdateWithFinalizingAnalysis() throws ResourceDoesNotExistException, SQLException, MethodNotAllowedException, IOException, URISyntaxException, ReadValueException, InvalidTypeForDoseCheckException, UnexpectedNumberOfResultsException {
    MetaBenefitRiskAnalysis analysis = new MetaBenefitRiskAnalysis(analysisId, projectId, "tittle");
    analysis.setFinalized(true);

    MetaBenefitRiskAnalysis oldAnalysis = new MetaBenefitRiskAnalysis(analysisId, projectId, "tittle");

    when(metaBenefitRiskAnalysisRepository.find(analysis.getId())).thenReturn(oldAnalysis);
    when(problemService.getProblem(projectId, analysisId)).thenReturn(null);

    metaBenefitRiskAnalysisService.update(user, projectId, analysis);

    verify(metaBenefitRiskAnalysisRepository).find(analysisId);
    verify(problemService).getProblem(projectId, analysisId);
    verify(scenarioRepository).create(analysisId, Scenario.DEFAULT_TITLE, "{\"problem\":null}");
    verify(metaBenefitRiskAnalysisRepository).update(user, analysis);
    verifyNoMoreInteractions(metaBenefitRiskAnalysisRepository, scenarioRepository, problemService);
  }

  @Test
  public void testUpdateWithNonFinalizingAnalysis() throws ResourceDoesNotExistException, SQLException, MethodNotAllowedException, IOException, URISyntaxException, ReadValueException, InvalidTypeForDoseCheckException, UnexpectedNumberOfResultsException {
    MetaBenefitRiskAnalysis analysis = new MetaBenefitRiskAnalysis(analysisId, projectId, "tittle");
    MetaBenefitRiskAnalysis oldAnalysis = new MetaBenefitRiskAnalysis(analysisId, projectId, "tittle");

    when(metaBenefitRiskAnalysisRepository.find(analysis.getId())).thenReturn(oldAnalysis);

    metaBenefitRiskAnalysisService.update(user, projectId, analysis);

    verify(metaBenefitRiskAnalysisRepository).find(analysisId);
    verify(metaBenefitRiskAnalysisRepository).update(user, analysis);
    verifyNoMoreInteractions(metaBenefitRiskAnalysisRepository, scenarioRepository, problemService);
  }

  @Test(expected = MethodNotAllowedException.class)
  public void testUpdateFinalizedAnalysisFails() throws ResourceDoesNotExistException, SQLException, MethodNotAllowedException, IOException, URISyntaxException, ReadValueException, InvalidTypeForDoseCheckException, UnexpectedNumberOfResultsException {
    MetaBenefitRiskAnalysis analysis = new MetaBenefitRiskAnalysis(analysisId, projectId, "tittle");
    MetaBenefitRiskAnalysis oldAnalysis = new MetaBenefitRiskAnalysis(analysisId, projectId, "tittle");
    oldAnalysis.setFinalized(true);

    when(metaBenefitRiskAnalysisRepository.find(analysis.getId())).thenReturn(oldAnalysis);

    metaBenefitRiskAnalysisService.update(user, projectId, analysis);
   }

}
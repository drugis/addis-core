package org.drugis.addis.analyses.service;

import com.google.common.collect.Sets;
import org.drugis.addis.analyses.model.InterventionInclusion;
import org.drugis.addis.analyses.model.BenefitRiskNMAOutcomeInclusion;
import org.drugis.addis.analyses.model.BenefitRiskAnalysis;
import org.drugis.addis.analyses.repository.BenefitRiskAnalysisRepository;
import org.drugis.addis.analyses.service.impl.BenefitRiskAnalysisServiceImpl;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ProblemCreationException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.interventions.model.SimpleIntervention;
import org.drugis.addis.interventions.repository.InterventionRepository;
import org.drugis.addis.interventions.service.impl.InvalidTypeForDoseCheckException;
import org.drugis.addis.patavitask.repository.UnexpectedNumberOfResultsException;
import org.drugis.addis.problems.service.ProblemService;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.scenarios.repository.ScenarioRepository;
import org.drugis.addis.security.Account;
import org.drugis.addis.subProblems.service.SubProblemService;
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
public class BenefitRiskAnalysisServiceTest {

  @Mock
  private BenefitRiskAnalysisRepository benefitRiskAnalysisRepository;

  @Mock
  private ScenarioRepository scenarioRepository;

  @Mock
  private SubProblemService subProblemService;

  @Mock
  private ProblemService problemService;

  @Mock
  private InterventionRepository interventionRepository;

  @Mock
  private ProjectService projectService;

  @InjectMocks
  private BenefitRiskAnalysisService benefitRiskAnalysisService = new BenefitRiskAnalysisServiceImpl();
  private final Account user = new Account("jondoe", "jon", "doe", "e@mail.com");
  private final Integer projectId = 1;
  private final Integer analysisId = 2;

  @Before
  public void setUp() {
    initMocks(this);
  }


  @Test
  public void testUpdateWithFinalizingAnalysis() throws ResourceDoesNotExistException, SQLException, MethodNotAllowedException, IOException, URISyntaxException, ReadValueException, InvalidTypeForDoseCheckException, UnexpectedNumberOfResultsException, ProblemCreationException {
    BenefitRiskAnalysis analysis = new BenefitRiskAnalysis(analysisId, projectId, "tittle");
    analysis.setFinalized(true);

    BenefitRiskAnalysis oldAnalysis = new BenefitRiskAnalysis(analysisId, projectId, "tittle");

    when(benefitRiskAnalysisRepository.find(analysis.getId())).thenReturn(oldAnalysis);
    when(problemService.getProblem(projectId, analysisId, null)).thenReturn(null);

    benefitRiskAnalysisService.update(user, projectId, analysis, "", null);

    verify(benefitRiskAnalysisRepository).find(analysisId);
    verify(problemService).getProblem(projectId, analysisId, null);
    verify(subProblemService).createMCDADefaults(projectId, analysisId, "");
    verify(benefitRiskAnalysisRepository).update(user, analysis);
    verifyNoMoreInteractions(benefitRiskAnalysisRepository, scenarioRepository, problemService);
  }

  @Test
  public void testUpdateWithNonFinalizingAnalysis() throws ResourceDoesNotExistException, SQLException, MethodNotAllowedException, IOException, URISyntaxException, ReadValueException, InvalidTypeForDoseCheckException, UnexpectedNumberOfResultsException, ProblemCreationException {
    BenefitRiskAnalysis analysis = new BenefitRiskAnalysis(analysisId, projectId, "tittle");
    BenefitRiskAnalysis oldAnalysis = new BenefitRiskAnalysis(analysisId, projectId, "tittle");

    when(benefitRiskAnalysisRepository.find(analysis.getId())).thenReturn(oldAnalysis);
    benefitRiskAnalysisService.update(user, projectId, analysis, "",null);

    verify(benefitRiskAnalysisRepository).find(analysisId);
    verify(benefitRiskAnalysisRepository).update(user, analysis);
    verifyNoMoreInteractions(benefitRiskAnalysisRepository, scenarioRepository, problemService);
  }

  @Test(expected = MethodNotAllowedException.class)
  public void testUpdateFinalizedAnalysisFails() throws ResourceDoesNotExistException, SQLException, MethodNotAllowedException, IOException, URISyntaxException, ReadValueException, InvalidTypeForDoseCheckException, UnexpectedNumberOfResultsException, ProblemCreationException {
    BenefitRiskAnalysis analysis = new BenefitRiskAnalysis(analysisId, projectId, "tittle");
    BenefitRiskAnalysis oldAnalysis = new BenefitRiskAnalysis(analysisId, projectId, "tittle");
    oldAnalysis.setFinalized(true);

    when(benefitRiskAnalysisRepository.find(analysis.getId())).thenReturn(oldAnalysis);

    benefitRiskAnalysisService.update(user, projectId, analysis, "",null);
  }

  @Test
  public void testCleanInclusions() {
    Integer outcomeId = -10;
    Integer nmaId = -100;
    Integer modelId = -1000;
    BenefitRiskAnalysis analysis = new BenefitRiskAnalysis(projectId, "title");
    BenefitRiskAnalysis oldAnalysis = new BenefitRiskAnalysis(projectId, "title");

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

    BenefitRiskNMAOutcomeInclusion outcomeInclusion = new BenefitRiskNMAOutcomeInclusion(analysisId, outcomeId, nmaId, modelId);
    outcomeInclusion.setBaseline("{\"name\": \"" + fluoxName + "\"}");
    analysis.setBenefitRiskNMAOutcomeInclusions(Collections.singletonList(outcomeInclusion));
    oldAnalysis.setBenefitRiskNMAOutcomeInclusions(Collections.singletonList(outcomeInclusion));

    when(interventionRepository.getByProjectIdAndName(projectId, fluoxName)).thenReturn(fluox);
    when(interventionRepository.getByProjectIdAndName(projectId, sertraName)).thenReturn(sertra);

    List<BenefitRiskNMAOutcomeInclusion> cleanInclusions = benefitRiskAnalysisService.removeBaselinesWithoutIntervention(analysis, oldAnalysis);

    assertEquals(Collections.singletonList(new BenefitRiskNMAOutcomeInclusion(analysisId, outcomeId, nmaId, modelId)), cleanInclusions);
  }
}
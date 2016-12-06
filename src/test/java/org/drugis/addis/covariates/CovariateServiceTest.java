package org.drugis.addis.covariates;

import org.drugis.addis.analyses.AbstractAnalysis;
import org.drugis.addis.analyses.CovariateInclusion;
import org.drugis.addis.analyses.NetworkMetaAnalysis;
import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.covariates.impl.CovariateServiceImpl;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.security.Account;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.social.OperationNotPermittedException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by joris on 6-12-16.
 */
public class CovariateServiceTest {

  @Mock
  AnalysisRepository analysisRepository;

  @Mock
  CovariateRepository covariateRepository;

  @Mock
  ProjectService projectService;

  @InjectMocks
  CovariateService covariateService;

  @Before
  public void setUp() {
    covariateService = new CovariateServiceImpl();
    initMocks(this);
  }

  @Test(expected = OperationNotPermittedException.class)
  public void testDeleteFailsIfCovariateIncluded() throws ResourceDoesNotExistException, MethodNotAllowedException {
    Account user = mock(Account.class);
    Integer projectId = 37;
    Integer analysisId1 = 42;
    Integer analysisId2 = 13;
    Integer covariateId = 7;
    CovariateInclusion covariateInclusion1 = new CovariateInclusion(analysisId1, covariateId);
    List<CovariateInclusion> covariateInclusions = Collections.singletonList(covariateInclusion1);
    AbstractAnalysis analysisWithInclusion = new NetworkMetaAnalysis(analysisId1, projectId, "analysisWithInclusion", Collections.emptyList(), Collections.emptyList(), covariateInclusions, null);
    AbstractAnalysis analysisWithoutInclusions = new NetworkMetaAnalysis(analysisId2, projectId, "analysisWithoutInclusion");
    List<AbstractAnalysis> analyses = Arrays.asList(analysisWithoutInclusions, analysisWithInclusion);
    when(analysisRepository.query(projectId)).thenReturn(analyses);

    covariateService.delete(user, projectId, covariateId);
  }

  @Test
  public void testDeleteSucceeds() throws ResourceDoesNotExistException, MethodNotAllowedException {
    Account user = mock(Account.class);
    Integer projectId = 37;
    Integer analysisId = 13;
    Integer covariateId = 7;
    AbstractAnalysis analysisWithoutInclusions = new NetworkMetaAnalysis(analysisId, projectId, "analysisWithoutInclusion");
    List<AbstractAnalysis> analyses = Collections.singletonList(analysisWithoutInclusions);
    when(analysisRepository.query(projectId)).thenReturn(analyses);

    covariateService.delete(user, projectId, covariateId);

    verify(covariateRepository).delete(covariateId);
  }
}
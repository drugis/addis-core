package org.drugis.addis.analyses;

import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.analyses.service.impl.AnalysisServiceImpl;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class AnalysisServiceTest {

  @Mock
  AnalysisRepository analysisRepository;

  @InjectMocks
  private AnalysisService analysisService;
  private Integer projectId = 1;
  private Integer analysisId = 2;

  @Before
  public void setUp() {
    analysisRepository = mock(AnalysisRepository.class);
    analysisService = new AnalysisServiceImpl();
    initMocks(this);
  }

  @Test
  public void testCheckCoordinatesSSBR() throws ResourceDoesNotExistException {
    SingleStudyBenefitRiskAnalysis singleStudyBenefitRiskAnalysis = mock(SingleStudyBenefitRiskAnalysis.class);
    when(singleStudyBenefitRiskAnalysis.getProjectId()).thenReturn(projectId);
    when(analysisRepository.get(projectId, analysisId)).thenReturn(singleStudyBenefitRiskAnalysis);

    analysisService.checkCoordinates(projectId, analysisId);

    verify(analysisRepository).get(projectId, analysisId);
    verifyNoMoreInteractions(analysisRepository);
  }

  @Test
  public void testCheckCoordinatesNMA() throws ResourceDoesNotExistException {
    NetworkMetaAnalysis networkMetaAnalysis = mock(NetworkMetaAnalysis.class);
    when(networkMetaAnalysis.getProjectId()).thenReturn(projectId);
    when(analysisRepository.get(projectId, analysisId)).thenReturn(networkMetaAnalysis);

    analysisService.checkCoordinates(projectId, analysisId);

    verify(analysisRepository).get(projectId, analysisId);
    verifyNoMoreInteractions(analysisRepository);
  }

  @Test(expected = ResourceDoesNotExistException.class)
  public void testCheckAnalysisNotInProject() throws ResourceDoesNotExistException {
    NetworkMetaAnalysis networkMetaAnalysis = mock(NetworkMetaAnalysis.class);
    when(analysisRepository.get(projectId, analysisId)).thenReturn(networkMetaAnalysis);

    analysisService.checkCoordinates(projectId, analysisId);
  }

}
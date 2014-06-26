package org.drugis.addis.models.service;

import org.drugis.addis.models.Model;
import org.drugis.addis.models.repository.ModelRepository;
import org.drugis.addis.models.service.impl.ModelServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Created by daan on 24-6-14.
 */
public class ModelServiceTest {


  @Mock
  private ModelRepository modelRepository;

  @InjectMocks
  private ModelService modelService;


  @Before
  public void setUp() throws Exception {
    modelService = new ModelServiceImpl();
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testQueryModelIsPresent() throws Exception {
    Integer analysisId = -1;
    Model model = new Model(-10, analysisId);
    when(modelRepository.findByAnalysis(analysisId)).thenReturn(model);
    List<Model> resultList = modelService.query(analysisId);
    assertEquals(1, resultList.size());
    assertEquals(model, resultList.get(0));
  }


  @Test
  public void testQueryModelIsNotPresent() throws Exception {
    Integer analysisId = -1;
    when(modelRepository.findByAnalysis(analysisId)).thenReturn(null);
    List<Model> resultList = modelService.query(analysisId);
    assertEquals(0, resultList.size());
  }

}

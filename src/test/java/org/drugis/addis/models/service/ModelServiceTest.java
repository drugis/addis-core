package org.drugis.addis.models.service;

import org.drugis.addis.models.Model;
import org.drugis.addis.models.repository.ModelRepository;
import org.drugis.addis.models.service.impl.ModelServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
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
    String modelTitle = "modelTitle";
    List<Model> models = Arrays.asList(new Model(-10, analysisId, modelTitle));
    when(modelRepository.findByAnalysis(analysisId)).thenReturn(models);
    List<Model> resultList = modelService.query(analysisId);
    assertEquals(1, resultList.size());
    assertEquals(models.get(0), resultList.get(0));
  }


  @Test
  public void testQueryModelIsNotPresent() throws Exception {
    Integer analysisId = -1;
    when(modelRepository.findByAnalysis(analysisId)).thenReturn(new ArrayList<Model>());
    List<Model> resultList = modelService.query(analysisId);
    assertEquals(0, resultList.size());
  }

}

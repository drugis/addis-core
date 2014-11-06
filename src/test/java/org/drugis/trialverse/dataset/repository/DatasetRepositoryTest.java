package org.drugis.trialverse.dataset.repository;

import com.hp.hpl.jena.query.DatasetAccessor;
import com.hp.hpl.jena.rdf.model.Model;
import org.drugis.trialverse.dataset.repository.impl.DatasetRepositoryImpl;
import org.drugis.trialverse.dataset.service.DatasetService;
import org.drugis.trialverse.security.Account;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class DatasetRepositoryTest {

  private static final java.lang.String DATASET_URI = "dataset uri";
  @Mock
  private DatasetService datasetService;

  @InjectMocks
  DatasetRepository datasetRepository;

  private
  DatasetAccessor datasetAccessor  = mock(DatasetAccessor.class);

  @Before
  public void setUp() {
    datasetRepository = new DatasetRepositoryImpl();
    MockitoAnnotations.initMocks(this);

    when(datasetService.getDatasetAccessor()).thenReturn(datasetAccessor);
    when(datasetService.createDatasetURI()).thenReturn(DATASET_URI);
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(datasetService);
  }

  @Test
  public void testCreateDataset() throws Exception {
    Account owner = new Account("my-owner", "fn", "ln");
    Model model = mock(Model.class);
    when(datasetService.createDatasetModel(owner, DATASET_URI)).thenReturn(model);
    String result = datasetRepository.createDataset("my-title", "my-description", owner);

    assertEquals(DATASET_URI, result);

    verify(datasetService).getDatasetAccessor();
    verify(datasetService).createDatasetURI();
    verify(datasetService).createDatasetModel(owner, DATASET_URI);
  }
}
package org.drugis.trialverse.dataset.repository;

import com.hp.hpl.jena.query.DatasetAccessor;
import com.hp.hpl.jena.rdf.model.Model;
import org.drugis.trialverse.dataset.factory.JenaFactory;
import org.drugis.trialverse.dataset.repository.impl.DatasetRepositoryImpl;
import org.drugis.trialverse.security.Account;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.*;

public class DatasetRepositoryTest {

  private static final java.lang.String DATASET_URI = "dataset uri";
  @Mock
  private JenaFactory jenaFactory;

  @InjectMocks
  DatasetRepository datasetRepository;

  private
  DatasetAccessor datasetAccessor = mock(DatasetAccessor.class);

  @Before
  public void setUp() {
    datasetRepository = new DatasetRepositoryImpl();
    MockitoAnnotations.initMocks(this);

    Model model = mock(Model.class);
    when(jenaFactory.getDatasetAccessor()).thenReturn(datasetAccessor);
    when(jenaFactory.createDatasetURI()).thenReturn(DATASET_URI);
    when(jenaFactory.createModel()).thenReturn(model);
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(jenaFactory);
  }

  @Test
  public void testCreateDataset() throws Exception {
    Account owner = new Account("my-owner", "fn", "ln");
    String result = datasetRepository.createDataset("my-title", "my-description", owner);

    assertEquals(DATASET_URI, result);

    verify(jenaFactory).getDatasetAccessor();
    verify(jenaFactory).createDatasetURI();
    verify(jenaFactory).createModel();
  }
}
package org.drugis.trialverse.dataset.repository;

import org.drugis.trialverse.dataset.repository.impl.DatasetRepositoryImpl;
import org.drugis.trialverse.security.Account;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertTrue;

public class DatasetRepositoryTest {

  @InjectMocks
  DatasetRepository datasetRepository;

  @Before
  public void setUp() {
    datasetRepository = new DatasetRepositoryImpl();
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testCreateDataset() throws Exception {
    String result = datasetRepository.createDataset("my-title", "my-description", new Account("my-owner", "fn", "ln"));
    assertTrue(result.startsWith(DatasetRepository.DATASET));
  }
}
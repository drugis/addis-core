package org.drugis.trialverse.dataset.service;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.drugis.addis.security.Account;
import org.drugis.trialverse.dataset.model.Dataset;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.repository.DatasetReadRepository;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.dataset.service.impl.DatasetServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by connor on 9/8/15.
 */
public class DatasetServiceTest {

  @Mock
  private DatasetReadRepository datasetReadRepository;

  @Mock
  private VersionMappingRepository versionMappingRepository;

  @InjectMocks
  private DatasetService datasetService;

  private Account account = new Account(1, "username", "John", "Lennon", "john@apple.co.uk");

  @Before
  public void setUp() {
    datasetService = new DatasetServiceImpl();
    initMocks(this);
  }

  @Test
  public void testFindDatasetsByUser() throws IOException {

    InputStream datasetsModelStream = new ClassPathResource("mockDatasetsModel.ttl").getInputStream();
    Model datasetsModel = ModelFactory.createDefaultModel();
    datasetsModel.read(datasetsModelStream, null, "TTL");

    VersionMapping versionMapping = new VersionMapping("versionDatadetUrl", "ownerUid", "http://trialverseDatasetUrl");
    List<VersionMapping> versionMappings = Arrays.asList(versionMapping);
    when(versionMappingRepository.findMappingsByEmail(account.getEmail())).thenReturn(versionMappings);
    when(datasetReadRepository.queryDataset(versionMapping)).thenReturn(datasetsModel);

    List<Dataset> datasets = datasetService.findDatasets(account);

    assertEquals(1, datasets.size());
    assertEquals("dataset 1", datasets.get(0).getTitle());
  }
}

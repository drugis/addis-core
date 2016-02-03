package org.drugis.trialverse.dataset.repository;

import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.repository.impl.VersionMappingRepositoryImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by connor on 11-3-15.
 */
public class VersionMappingRepositoryTest {

  @Mock
  private JdbcTemplate jdbcTemplate;


  @InjectMocks
  VersionMappingRepository versionMappingRepository;

  String userName = "userName";

  @Before
  public void setUp() {
    versionMappingRepository = new VersionMappingRepositoryImpl();
    MockitoAnnotations.initMocks(this);
  }


  @Test
  public void testCreateMapping() {
    String datasetLocation = "datasetLocation";
    String ownerUuid = "ownerUuid";
    String trialverseDataset = "versionKey";

    VersionMapping versionMapping = new VersionMapping(datasetLocation, ownerUuid, trialverseDataset);

    versionMappingRepository.save(versionMapping);

    verify(jdbcTemplate).update("insert into VersionMapping (versionedDatasetUrl, ownerUuid, trialverseDatasetUrl) values (?, ?, ?)",
            datasetLocation, ownerUuid, trialverseDataset);
  }

  @Test
  public void testFindMappingsByUserName() {
    List<Object> mockResult = Arrays.asList(new VersionMapping(1, "datasetUui1", userName, "trialverseDataset"));
    when(jdbcTemplate.query(anyString(), any(Object[].class), any(RowMapper.class))).thenReturn(mockResult);

    List<VersionMapping> mappings = versionMappingRepository.findMappingsByEmail(userName);

    assertEquals(mockResult, mappings);
  }

  @Test
  public void getVersionUrlByDatasetUrl() throws URISyntaxException {
    URI datasetUrl = new URI("datasetUrl");
    VersionMapping mockResult = new VersionMapping(1, "datasetUui1", userName, "trialverseDataset");
    when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), anyObject())).thenReturn(mockResult);

    VersionMapping versionMapping = versionMappingRepository.getVersionMappingByDatasetUrl(datasetUrl);

    assertEquals(mockResult, versionMapping);
  }

  @Test
  public void testGetVersionMappings() {
    List<Object> mockResult = Arrays.asList(new VersionMapping(1, "datasetUui1", userName, "trialverseDataset"));
    when(jdbcTemplate.query(anyString(), any(Object[].class), any(RowMapper.class))).thenReturn(mockResult);

    List<VersionMapping> mappings = versionMappingRepository.getVersionMappings();

    assertEquals(mockResult, mappings);
    versionMappingRepository.getVersionMappings();
  }

  @Test
  public void getVersionMappingByVersionedUrl() throws URISyntaxException {
    URI datasetUrl = new URI("datasetUrl");
    VersionMapping mockResult = new VersionMapping(1, "datasetUui1", userName, "trialverseDataset");
    when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), anyObject())).thenReturn(mockResult);
    VersionMapping versionMapping = versionMappingRepository.getVersionMappingByVersionedURl(datasetUrl);

    assertEquals(mockResult, versionMapping);
  }

}

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

import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.Assert.*;

/**
 * Created by connor on 11-3-15.
 */
public class VersionMappingRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;


    @InjectMocks
    VersionMappingRepository versionMappingRepository;


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

        verify(jdbcTemplate).update("insert into VersionMapping (datasetLocation, ownerUuid, trialverseDataset) values (?, ?, ?)",
                datasetLocation, ownerUuid, trialverseDataset);
    }

    @Test
    public void testFindMappingsByUserName() {
        String userName = "userName";

        List<VersionMapping> mockResult = Arrays.asList(new VersionMapping(1, "datasetUui1", userName, "trialverseDataset"));
        when(jdbcTemplate.query(anyString(), any(Object[].class), any(RowMapper.class))).thenReturn(mockResult);

        List<VersionMapping> mappings = versionMappingRepository.findMappingsByUsername(userName);

        assertEquals(mockResult, mappings);
    }
}

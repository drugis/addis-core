package org.drugis.trialverse.dataset.repository;

import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.repository.impl.VersionMappingRepositoryImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.Mockito.verify;

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

        String datasetUuid = "datasetUuid";
        String ownerUuid = "ownerUuid";
        String versionKey = "versionKey";

        VersionMapping versionMapping = new VersionMapping(datasetUuid, ownerUuid, versionKey);

        versionMappingRepository.createMapping(versionMapping);

        verify(jdbcTemplate).update("insert into VersionMapping (datasetUuid, ownerUuid, versionKey) values (?, ?, ?)",
                datasetUuid, ownerUuid, versionKey);
    }
}

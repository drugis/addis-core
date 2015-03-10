package org.drugis.trialverse.dataset.repository;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.query.DatasetAccessor;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.drugis.trialverse.dataset.factory.JenaFactory;
import org.drugis.trialverse.dataset.repository.impl.DatasetWriteRepositoryImpl;
import org.drugis.trialverse.security.Account;
import org.drugis.trialverse.util.WebConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URI;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.*;

public class DatasetWriteRepositoryTest {

    private static final java.lang.String DATASET_URI = "http://localhost:8080";

    @Mock
    private WebConstants webConstants;

    @Mock
    private VersionMappingRepository versionMappingRepository;

    @InjectMocks
    DatasetWriteRepository datasetWriteRepository;


    @Before
    public void setUp() {
        datasetWriteRepository = new DatasetWriteRepositoryImpl();
        MockitoAnnotations.initMocks(this);

        when(webConstants.getTriplestoreBaseUri()).thenReturn(DATASET_URI);

    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(webConstants);
    }

    @Test
    public void testCreateDataset() throws Exception {
        Account owner = new Account("my-owner", "fn", "ln");
        String title = "my-title";
        String description = "description";
        URI result = datasetWriteRepository.createDataset(title, description, owner);

        assertTrue(result.toString().startsWith(DATASET_URI + "/datasets"));

        verify(webConstants).getTriplestoreBaseUri();
    }

    @Ignore
    @Test
    public void testCreateDatasetWithNullDescription() throws Exception {
        Account owner = new Account("my-owner", "fn", "ln");
        String title = "my-title";
        String description = null;
        URI result = datasetWriteRepository.createDataset(title, description, owner);

        assertEquals(DATASET_URI, result);

        verify(webConstants).getTriplestoreDataUri();



    }
}
package org.drugis.trialverse.dataset.repository;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.query.DatasetAccessor;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.drugis.trialverse.dataset.factory.JenaFactory;
import org.drugis.trialverse.dataset.repository.impl.DatasetWriteRepositoryImpl;
import org.drugis.trialverse.security.Account;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.*;

public class DatasetWriteRepositoryTest {

  private static final java.lang.String DATASET_URI = "dataset uri";
  @Mock
  private JenaFactory jenaFactory;

  @InjectMocks
  DatasetWriteRepository datasetWriteRepository;

  private DatasetAccessor datasetAccessor = mock(DatasetAccessor.class);

  private Model model;

  @Before
  public void setUp() {
    datasetWriteRepository = new DatasetWriteRepositoryImpl();
    MockitoAnnotations.initMocks(this);

    model = ModelFactory.createDefaultModel();

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
    String title = "my-title";
    String description = "description";
    String result = datasetWriteRepository.createDataset(title, description, owner);

    assertEquals(DATASET_URI, result);

    assertEquals(DATASET_URI, model.getResource(DATASET_URI).getURI());
    assertEquals(title, model.getRDFNode(NodeFactory.createLiteral(title)).toString());
    assertEquals(owner.getUsername(), model.getRDFNode(NodeFactory.createLiteral(owner.getUsername())).toString());
    assertEquals(description, model.getRDFNode(NodeFactory.createLiteral(description)).toString());

    verify(jenaFactory).getDatasetAccessor();
    verify(jenaFactory).createDatasetURI();
    verify(jenaFactory).createModel();
  }

  @Test
  public void testCreateDatasetWithNullDescription() throws Exception {
    Account owner = new Account("my-owner", "fn", "ln");
    String title = "my-title";
    String description = null;
    String result = datasetWriteRepository.createDataset(title, description, owner);

    assertEquals(DATASET_URI, result);

    assertEquals(DATASET_URI, model.getResource(DATASET_URI).getURI());
    assertEquals(title, model.getRDFNode(NodeFactory.createLiteral(title)).toString());
    assertEquals(owner.getUsername(), model.getRDFNode(NodeFactory.createLiteral(owner.getUsername())).toString());
    String ontologyDataset = "ontology:Dataset";
    assertEquals(ontologyDataset, model.getRDFNode(NodeFactory.createLiteral(ontologyDataset)).toString());

    verify(jenaFactory).getDatasetAccessor();
    verify(jenaFactory).createDatasetURI();
    verify(jenaFactory).createModel();


  }
}
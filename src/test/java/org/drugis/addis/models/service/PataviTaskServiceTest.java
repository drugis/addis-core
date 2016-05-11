package org.drugis.addis.models.service;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.service.impl.InvalidTypeForDoseCheckException;
import org.drugis.addis.models.Model;
import org.drugis.addis.models.exceptions.InvalidModelException;
import org.drugis.addis.models.repository.ModelRepository;
import org.drugis.addis.patavitask.PataviTask;
import org.drugis.addis.patavitask.PataviTaskUriHolder;
import org.drugis.addis.patavitask.repository.PataviTaskRepository;
import org.drugis.addis.patavitask.repository.impl.PataviTaskRepositoryImpl;
import org.drugis.addis.patavitask.service.PataviTaskService;
import org.drugis.addis.patavitask.service.impl.PataviTaskServiceImpl;
import org.drugis.addis.problems.model.NetworkMetaAnalysisProblem;
import org.drugis.addis.problems.service.ProblemService;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.drugis.addis.trialverse.service.impl.ReadValueException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.*;
import java.security.cert.CertificateException;
import java.sql.SQLException;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

;

public class PataviTaskServiceTest {
  @Mock
  private TriplestoreService triplestoreService;

  @Mock
  private ProblemService problemService;

  @Mock
  private ModelRepository modelRepository;

  @Mock
  private PataviTaskRepository pataviTaskRepository;

  @InjectMocks
  private PataviTaskService pataviTaskService;


  @Before
  public void setUp() throws ResourceDoesNotExistException {
    pataviTaskService = new PataviTaskServiceImpl();
    initMocks(this);
    reset(modelRepository, pataviTaskRepository, problemService);
  }

  @After
  public void cleanUp() {
    verifyNoMoreInteractions(modelRepository, pataviTaskRepository);
  }

  @Test
  public void testFindTaskWhenThereIsNoTask() throws ResourceDoesNotExistException, IOException, SQLException, InvalidModelException, URISyntaxException, ReadValueException, InvalidTypeForDoseCheckException, UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
    Integer modelId = -2;
    String problem = "Yo";
    Integer projectId = -6;
    Integer analysisId = -7;
    String modelTitle = "modelTitle";
    String linearModel = Model.LINEAR_MODEL_FIXED;
    Integer burnInIterations = 5000;
    Integer inferenceIterations = 20000;
    Integer thinningFactor = 10;
    String likelihood = Model.LIKELIHOOD_BINOM;
    String link = Model.LINK_LOG;

    Model model = new Model.ModelBuilder(analysisId, modelTitle)
            .id(modelId)
            .linearModel(linearModel)
            .modelType(Model.NETWORK_MODEL_TYPE)
            .burnInIterations(burnInIterations)
            .inferenceIterations(inferenceIterations)
            .thinningFactor(thinningFactor)
            .likelihood(likelihood)
            .link(link)
            .build();
    NetworkMetaAnalysisProblem networkMetaAnalysisProblem = mock(NetworkMetaAnalysisProblem.class);
    PataviTask pataviTask = new PataviTask(PataviTaskRepositoryImpl.GEMTC_METHOD, problem);
    when(networkMetaAnalysisProblem.toString()).thenReturn(problem);
    when(problemService.getProblem(projectId, analysisId)).thenReturn(networkMetaAnalysisProblem);
    when(modelRepository.find(modelId)).thenReturn(model);
    URI createdURI = URI.create("new.task.com");
    when(pataviTaskRepository.createPataviTask(networkMetaAnalysisProblem.buildProblemWithModelSettings(model))).thenReturn(createdURI);

    PataviTaskUriHolder result = pataviTaskService.getPataviTaskUriHolder(projectId, analysisId, modelId);

    assertNotNull(result.getUri());
    verify(modelRepository).find(modelId);
    verify(problemService).getProblem(projectId, analysisId);
    verify(pataviTaskRepository).createPataviTask(networkMetaAnalysisProblem.buildProblemWithModelSettings(model));
  }

  @Test
  public void testFindTaskWhenThereAlreadyIsATask() throws ResourceDoesNotExistException, IOException, SQLException, InvalidModelException, URISyntaxException, ReadValueException, InvalidTypeForDoseCheckException, UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
    Integer modelId = -2;
    Integer projectId = -6;
    Integer analysisId = -7;
    String modelTitle = "modelTitle";
    String linearModel = Model.LINEAR_MODEL_FIXED;
    String modelType = Model.NETWORK_MODEL_TYPE;
    Integer burnInIterations = 5000;
    Integer inferenceIterations = 20000;
    Integer thinningFactor = 10;
    String likelihood = Model.LIKELIHOOD_BINOM;
    String link = Model.LINK_LOG;

    Model model = new Model.ModelBuilder(analysisId, modelTitle)
            .id(modelId)
            .taskUri(URI.create("-7"))
            .linearModel(linearModel)
            .modelType(modelType)
            .burnInIterations(burnInIterations)
            .inferenceIterations(inferenceIterations)
            .thinningFactor(thinningFactor)
            .likelihood(likelihood)
            .link(link)
            .build();
    when(modelRepository.find(modelId)).thenReturn(model);

    PataviTaskUriHolder result = pataviTaskService.getPataviTaskUriHolder(projectId, analysisId, modelId);
    assertNotNull(result.getUri());
    verify(modelRepository).find(modelId);
  }

  @Test(expected = ResourceDoesNotExistException.class)
  public void testFindTaskForInvalidModel() throws ResourceDoesNotExistException, IOException, SQLException, InvalidModelException, URISyntaxException, ReadValueException, InvalidTypeForDoseCheckException, UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
    Integer projectId = -6;
    Integer analysisId = -7;
    Integer invalidModelId = -2;
    when(modelRepository.find(invalidModelId)).thenReturn(null);
    try{
      pataviTaskService.getPataviTaskUriHolder(projectId, analysisId, invalidModelId);
    }finally {
      verify(modelRepository).find(invalidModelId);
    }
  }

  @Test
  public void testConnect() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {
    System.setProperty("javax.net.ssl.trustStore", "/home/daan/certs/drugis-ca.jks");

    // read in the keystore from the filesystem, this should contain a single keypair
    KeyStore clientKeyStore = KeyStore.getInstance("JKS");
    String pwd = "develop";
    clientKeyStore.load(new FileInputStream("/home/daan/certs/addis-daan.jks"),  pwd.toCharArray());

    SSLContext sslContext = SSLContexts
            .custom()
            .loadKeyMaterial(clientKeyStore, pwd.toCharArray())
            .build();
    SSLConnectionSocketFactory confac = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

    HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

    Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
            .register("https", confac)
            .build();
    HttpClientConnectionManager ccm = new BasicHttpClientConnectionManager(registry);

    httpClientBuilder.setConnectionManager(ccm);

    CloseableHttpClient client = httpClientBuilder.build();

    // create the method to execute
    HttpGet m = new HttpGet("https://basil.spice.drugis.org:3000");

    // execute the method
    HttpResponse response = client.execute(m);
    assertNotNull(response);
  }


}

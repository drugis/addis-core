package org.drugis.trialverse.graph.service;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.ApiKey;
import org.drugis.addis.security.AuthenticationService;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.repository.DatasetReadRepository;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.graph.service.impl.GraphServiceImpl;
import org.drugis.trialverse.security.TrialversePrincipal;
import org.drugis.trialverse.util.Namespaces;
import org.drugis.addis.util.WebConstants;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;

import static java.nio.charset.Charset.defaultCharset;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by daan on 26-8-15.
 */
public class GraphServiceTest {

  @InjectMocks
  GraphService graphService;

  @Mock
  AccountRepository accountRepository;

  @Mock
  AuthenticationService authenticationService;

  @Mock
  VersionMappingRepository versionMappingRepository;

  @Mock
  DatasetReadRepository datasetReadRepository;

  @Mock
  WebConstants webConstants;

  @Mock
  RestTemplate restTemplate;

  private String testHost = "http://localhost:8080";

  @Before
  public void setUp() {
    graphService = new GraphServiceImpl();
    initMocks(this);
    when(webConstants.getTriplestoreBaseUri()).thenReturn("http://something.com");
  }

  @Test
  public void testCopy() throws Exception {
    Account owner = new Account("my-owner", "fn", "ln", "unh");
    String targetDatasetUri = "http://target.dataset";
    URI targetGraphUri = new URI("http://target.graph.uri");
    String sourceDatasetUuid = "sourceDatasetUuid";
    URI sourceGraphUri = new URI(testHost + "/datasets/" + sourceDatasetUuid + "/versions/headVersion/graphs/totallyCoolGraph");
    URI sourceDatasetUri = new URI(Namespaces.DATASET_NAMESPACE + sourceDatasetUuid);
    URI revisionUri = new URI(testHost + "/revisions/headRevisionOfTotallyCoolGraph");

    Model historyModel = ModelFactory.createDefaultModel();
    InputStream historyStream = new ClassPathResource("mockHistory.ttl").getInputStream();
    historyModel.read(historyStream, null, "TTL");
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(WebConstants.X_EVENT_SOURCE_VERSION, "newVersion");
    ResponseEntity responseEntity = new ResponseEntity(httpHeaders, HttpStatus.OK);
    String targetDatasetVersionedUri = testHost + "/datasets/targetId";
    VersionMapping targetMapping = new VersionMapping(targetDatasetVersionedUri, null, targetDatasetUri);
    String sourceDatasetVersionedUrl =  testHost + "/datasets/sourceId";
    VersionMapping sourceMapping = new VersionMapping(sourceDatasetVersionedUrl, null, sourceDatasetUri.toString());
    URI uri = UriComponentsBuilder.fromHttpUrl(targetDatasetUri)
            .path(WebConstants.DATA_ENDPOINT)
            .queryParam(WebConstants.COPY_OF_QUERY_PARAM, revisionUri.toString())
            .queryParam(WebConstants.GRAPH_QUERY_PARAM, targetGraphUri.toString())
            .build()
            .toUri();
    when(versionMappingRepository.getVersionMappingByDatasetUrl(URI.create(targetDatasetUri))).thenReturn(targetMapping);
    when(versionMappingRepository.getVersionMappingByDatasetUrl(sourceDatasetUri)).thenReturn(sourceMapping);
    ApiKey apiKey = mock(ApiKey.class);
    TrialversePrincipal trialversePrincipal = new TrialversePrincipal(new PreAuthenticatedAuthenticationToken(owner, apiKey));
    when(authenticationService.getAuthentication()).thenReturn(trialversePrincipal);
    when(datasetReadRepository.getHistory(sourceMapping.getVersionedDatasetUri())).thenReturn(historyModel);
    HttpHeaders headers = new HttpHeaders();
    headers.add(WebConstants.EVENT_SOURCE_CREATOR_HEADER, "https://trialverse.org/apikeys/0");
    headers.add(WebConstants.EVENT_SOURCE_TITLE_HEADER, Base64.encodeBase64String("Study copied from other dataset".getBytes()));
    when(restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(headers), String.class)).thenReturn(responseEntity);
    when( accountRepository.findAccountByUsername(trialversePrincipal.getUserName())).thenReturn(owner);

    URI newVersion = graphService.copy(URI.create(targetDatasetUri), targetGraphUri, sourceGraphUri);

    assertEquals("newVersion", newVersion.toString());
  }

  @Test
  public void testExtractDatasetUri() {
    URI uri = URI.create("https://www.trialverse123.org/datasets/333-av-3222/versions/434-334/graphs/44334");
    URI datasetUri = graphService.extractDatasetUri(uri);
    assertEquals(Namespaces.DATASET_NAMESPACE + "333-av-3222", datasetUri.toString());
  }

  @Test
  public void testExtractVersionUri() {
    URI uri = URI.create("https://www.trialverse123.org/datasets/333-av-3222/versions/434-334/graphs/44334");
    URI versionUri = graphService.extractVersionUri(uri);
    assertEquals(testHost + "/versions/" + "434-334", versionUri.toString());
  }

  @Test
  public void testExtractGraphUri() {
    URI uri = URI.create("https://www.trialverse123.org/datasets/333-av-3222/versions/434-334/graphs/443-34");
    URI graphUri = graphService.extractGraphUri(uri);
    assertEquals(Namespaces.GRAPH_NAMESPACE + "443-34", graphUri.toString());
  }

  @Test
  public void testJsonToTurtle() throws IOException {
    String source = "{\"@graph\":[{\"@id\":\"http://trials.drugis.org/studies/695855bd-5782-4c67-a270-eb4459c3a4f6\",\"@type\":\"ontology:Study\",\"has_epochs\":[],\"comment\":\"my study\",\"label\":\"study 1\",\"has_outcome\":[],\"has_arm\":[{\"label\":\"jt\",\"comment\":\"set\",\"@id\":\"http://trials.drugis.org/instances/87e3e348-da19-4639-94b2-4cf8b547b976\",\"@type\":\"ontology:Arm\"}],\"has_activity\":[],\"has_indication\":[],\"has_objective\":[],\"has_publication\":[],\"has_eligibility_criteria\":[]}],\"@context\":{\"label\":\"http://www.w3.org/2000/01/rdf-schema#label\",\"comment\":\"http://www.w3.org/2000/01/rdf-schema#comment\",\"has_epochs\":{\"@id\":\"http://trials.drugis.org/ontology#has_epochs\",\"@container\":\"@list\"},\"ontology\":\"http://trials.drugis.org/ontology#\"}}";
    InputStream is = new ByteArrayInputStream(source.getBytes());
    InputStream resultStream = graphService.jsonGraphInputStreamToTurtleInputStream(is);
    StringWriter writer = new StringWriter();
    IOUtils.copy(resultStream, writer, defaultCharset());
    String result = writer.toString();
    assertEquals("@prefix ontology: <http://trials.drugis.org/ontology#> .\n" +
            "\n" +
            "<http://trials.drugis.org/studies/695855bd-5782-4c67-a270-eb4459c3a4f6>\n" +
            "        a                    ontology:Study ;\n" +
            "        <http://www.w3.org/2000/01/rdf-schema#comment>\n" +
            "                \"my study\" ;\n" +
            "        <http://www.w3.org/2000/01/rdf-schema#label>\n" +
            "                \"study 1\" ;\n" +
            "        ontology:has_epochs  () .", result.trim());
  }

}
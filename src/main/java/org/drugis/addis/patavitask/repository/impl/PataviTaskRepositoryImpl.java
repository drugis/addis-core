package org.drugis.addis.patavitask.repository.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContexts;
import org.drugis.addis.models.Model;
import org.drugis.addis.patavitask.PataviTask;
import org.drugis.addis.patavitask.repository.PataviTaskRepository;
import org.drugis.addis.patavitask.repository.UnexpectedNumberOfResultsException;
import org.drugis.addis.problems.model.NetworkMetaAnalysisProblem;
import org.drugis.addis.util.WebConstants;
import org.drugis.trialverse.util.Namespaces;
import org.json.JSONObject;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.net.ssl.SSLContext;
import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.*;
import java.security.cert.CertificateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by connor on 26-6-14.
 */
@Repository
public class PataviTaskRepositoryImpl implements PataviTaskRepository {
  public final static String GEMTC_METHOD = "gemtc";
  final static Logger logger = LoggerFactory.getLogger(PataviTaskRepositoryImpl.class);
  private final static String SELECTOR_PART = "SELECT id, method, problem, result IS NOT NULL as hasResult";
  private ObjectMapper objectMapper = new ObjectMapper();

  @Inject
  WebConstants webConstants;

  @Override
  public URI createPataviTask(NetworkMetaAnalysisProblem problem, Model model) throws JsonProcessingException {
    logger.trace("PataviTaskRepositoryImpl.createPataviTask");

    String problemString = objectMapper.writeValueAsString(problem);
    JSONObject jsonProblem = new JSONObject(problemString);
    jsonProblem.put("linearModel", model.getLinearModel());
    jsonProblem.put("modelType", new JSONObject(objectMapper.writeValueAsString(model.getModelType())));
    jsonProblem.put("burnInIterations", model.getBurnInIterations());
    jsonProblem.put("inferenceIterations", model.getInferenceIterations());
    jsonProblem.put("thinningFactor", model.getThinningFactor());
    jsonProblem.put("likelihood", model.getLikelihood());
    jsonProblem.put("link", model.getLink());
    jsonProblem.put("regressor", model.getRegressor());

    if (model.getHeterogeneityPrior() != null) {
      jsonProblem.put("heterogeneityPrior", new JSONObject(objectMapper.writeValueAsString(model.getHeterogeneityPrior())));
    }

    if (model.getSensitivity() != null) {
      jsonProblem.put("sensitivity", model.getSensitivity());
    }

    if (model.getOutcomeScale() != null) {
      jsonProblem.put("outcomeScale", model.getOutcomeScale());
    }

//    function createPataviTask(problem, callback) {
//      logger.debug('pataviTaskRepository.createPataviTask');
//      var reqOptions = {
//              path: '/task?service=gemtc',
//              method: 'POST',
//              headers: {
//        'Content-Type': 'application/json',
//      }
//      };
//      var postReq = https.request(_.extend(httpsOptions, reqOptions), function(res) {
//        if (res.statusCode === httpStatus.OK && res.headers.location) {
//          callback(null, res.headers.location);
//        } else {
//          callback('Error queueing task: server returned code ' + res.statusCode);
//        }
//      });
//      postReq.write(JSON.stringify(problem));
//      postReq.end();
//    }

    URI uri = null;
    try {
      URIBuilder builder = new URIBuilder(webConstants.getPataviUri());
      builder.setPath("/task");
      builder.addParameter("service", "gemtc");
      uri = builder.build();
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
    HttpPost postRequest = new HttpPost(uri);
    postRequest.addHeader(new BasicHeader("Content-type", WebConstants.APPLICATION_JSON_UTF8_VALUE));
    HttpEntity postBody = new ByteArrayEntity(jsonProblem.toString().getBytes());
    postRequest.setEntity(postBody);
    try {
      HttpResponse httpResponse = sslRequest(postRequest);
      URI newTaskUri = URI.create(httpResponse.getHeaders("Location")[0].getValue());
      logger.debug("created new patavi-task with taskUri = " + newTaskUri.toString());
      return newTaskUri;
    } catch (Exception e) {
      throw new RuntimeException("Error creating patavi task: " + e.toString());
    }
  }

  @Override
  public List<PataviTask> findByIds(List<URI> taskUris) throws SQLException {

    // Use different query for live psql db as psql does accept a set as part of the in clause
    boolean isHsqlDrive = dataSource instanceof EmbeddedDatabase;
    String query;
    if (isHsqlDrive) {
      query = SELECTOR_PART + " FROM patavitask WHERE id IN(UNNEST(?)) ";
    } else {
      query = SELECTOR_PART + " FROM patavitask WHERE id IN(select(UNNEST(?))) ";
    }

    try (Connection connection = dataSource.getConnection()) {
      final PreparedStatement statement = connection.prepareStatement(query);
      statement.setArray(1, connection.createArrayOf("varchar", taskUris.toArray()));

      List<PataviTask> result = new ArrayList<>();
      int i = 0;
      ResultSet rs = statement.executeQuery();
      while (rs.next()) {
        result.add(rowMapper.mapRow(rs, i));
        ++i;
      }
      return result;
    }
  }

  @Override
  public JsonNode getResult(URI taskId) throws IOException, UnexpectedNumberOfResultsException {
    String result = jdbcTemplate.queryForObject("SELECT result FROM patavitask where id = " + taskId, String.class);
    if (result == null) {
      throw new UnexpectedNumberOfResultsException("expected was 1 but got zero results");
    }
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.readTree(result).get("results");
  }

  @Override
  public Map<Integer, JsonNode> getResults(List<URI> taskIds) throws SQLException, IOException {
    // Use different query for live psql db as psql does accept a set as part of the in clause
    boolean isHsqlDrive = dataSource instanceof EmbeddedDatabase;
    String query;
    if (isHsqlDrive) {
      query = "SELECT id, result FROM patavitask WHERE id IN(UNNEST(?)) ";
    } else {
      query = "SELECT id, result FROM patavitask WHERE id IN(select(UNNEST(?))) ";
    }

    try (Connection connection = dataSource.getConnection()) {
      final PreparedStatement statement = connection.prepareStatement(query);
      statement.setArray(1, connection.createArrayOf("int", taskIds.toArray()));

      ObjectMapper objectMapper = new ObjectMapper();

      ResultSet rs = statement.executeQuery();
      Map<Integer, JsonNode> result = new HashMap<>();
      while (rs.next()) {
        result.put(rs.getInt("id"), objectMapper.readTree(rs.getString("result")).get("results"));
      }
      return result;
    }
  }

  private HttpResponse sslRequest(HttpRequestBase request) throws KeyStoreException, IOException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyManagementException, CertificateException {
    System.setProperty("javax.net.ssl.trustStore", "/home/daan/certs/drugis-ca.jks");

    // read in the keystore from the filesystem, this should contain a single keypair
    KeyStore clientKeyStore = KeyStore.getInstance("JKS");
    String pwd = "develop";
    clientKeyStore.load(new FileInputStream("/home/daan/certs/addis-daan.jks"),  pwd.toCharArray());

    SSLContext sslContext = SSLContexts
            .custom()
            .loadKeyMaterial(clientKeyStore, pwd.toCharArray())
            .build();
    SSLConnectionSocketFactory connectionSocketFactory = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

    HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

    Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
            .register("https", connectionSocketFactory)
            .build();
    HttpClientConnectionManager clientConnectionManager = new BasicHttpClientConnectionManager(registry);

    httpClientBuilder.setConnectionManager(clientConnectionManager);

    CloseableHttpClient client = httpClientBuilder.build();

    // execute the method
    return client.execute(request);
  }

}

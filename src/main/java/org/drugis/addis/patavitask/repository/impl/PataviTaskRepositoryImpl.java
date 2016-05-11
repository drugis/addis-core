package org.drugis.addis.patavitask.repository.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHeader;
import org.drugis.addis.patavitask.PataviTask;
import org.drugis.addis.patavitask.repository.PataviTaskRepository;
import org.drugis.addis.util.WebConstants;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by connor on 26-6-14.
 */
@Repository
public class PataviTaskRepositoryImpl implements PataviTaskRepository {
  public final static String GEMTC_METHOD = "gemtc";
  final static Logger logger = LoggerFactory.getLogger(PataviTaskRepositoryImpl.class);
  private ObjectMapper objectMapper = new ObjectMapper();

  @Inject
  WebConstants webConstants;

  @Inject
  HttpClient httpClient;

  @Override
  public URI createPataviTask(JSONObject jsonProblem) {
    logger.trace("PataviTaskRepositoryImpl.createPataviTask");

    HttpPost postRequest = new HttpPost(webConstants.getPataviGemtcUri());
    postRequest.addHeader(new BasicHeader("Content-type", WebConstants.APPLICATION_JSON_UTF8_VALUE));
    HttpEntity postBody = new ByteArrayEntity(jsonProblem.toString().getBytes());
    postRequest.setEntity(postBody);
    try {
      HttpResponse httpResponse = httpClient.execute(postRequest);
      URI newTaskUri = URI.create(httpResponse.getHeaders("Location")[0].getValue());
      logger.debug("created new patavi-task with taskUri = " + newTaskUri.toString());
      return newTaskUri;
    } catch (Exception e) {
      throw new RuntimeException("Error creating patavi task: " + e.toString());
    }
  }

  @Override
  public JsonNode getResult(URI taskUri) throws URISyntaxException, IOException {
    URI resultsUri = new URIBuilder(taskUri)
            .setPath(WebConstants.PATAVI_RESULTS_PATH)
            .build();
    HttpGet getRequest = new HttpGet(resultsUri);
    HttpResponse response = httpClient.execute(getRequest);
    return objectMapper.readTree(response.getEntity().getContent().toString());
  }

  @Override
  public Map<URI, JsonNode> getResults(List<URI> taskUris) throws URISyntaxException, IOException {
    List<URI> filteredUris = taskUris.stream()
            .filter(uri -> uri != null)
            .collect(Collectors.toList());

    Map<URI, JsonNode> result = new HashMap<>();
    for (URI uri : filteredUris) {
      result.put(uri, getResult(uri));
    }
    return result;
  }

  @Override
  public HttpResponse delete(URI taskUrl) throws IOException {
    return httpClient.execute(new HttpDelete(taskUrl));
  }

  @Override
  public PataviTask getTask(URI taskUrl) throws IOException {
    HttpResponse response = httpClient.execute(new HttpGet(taskUrl));
    return new PataviTask(response.getEntity().getContent().toString());
  }

  @Override
  public Map<URI, Boolean> getRunStatus(List<URI> taskUris) {
    return null;
  }

  @Override
  public List<PataviTask> findByIds(List<URI> taskUris) throws SQLException {
//    taskUris.stream().collect(Collectors.toMap(Function.identity(), uri -> {
////      sslRequest()
//    }));
    return null;
  }

}

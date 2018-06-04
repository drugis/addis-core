package org.drugis.addis.patavitask.repository.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.drugis.addis.patavitask.PataviTask;
import org.drugis.addis.patavitask.PataviTaskUriHolder;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by connor on 26-6-14.
 */
@Repository
public class PataviTaskRepositoryImpl implements PataviTaskRepository {
  private final static Logger logger = LoggerFactory.getLogger(PataviTaskRepositoryImpl.class);
  private ObjectMapper objectMapper = new ObjectMapper();

  @Inject
  private WebConstants webConstants;

  @Inject
  private HttpClient httpClient;

  @Override
  public URI createPataviTask(URI pataviUri, JSONObject jsonProblem) {
    logger.trace("PataviTaskRepositoryImpl.createPataviTask");

    HttpPost postRequest = new HttpPost(pataviUri);
    postRequest.addHeader(new BasicHeader("Content-type", WebConstants.APPLICATION_JSON_UTF8_VALUE));
    HttpEntity postBody = new ByteArrayEntity(jsonProblem.toString().getBytes());
    postRequest.setEntity(postBody);
    try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) httpClient.execute(postRequest)) {
      String location = httpResponse.getHeaders("Location")[0].getValue();
      URI newTaskUri = URI.create(location);
      logger.debug("created new patavi-task with taskUri = " + newTaskUri.toString());
      EntityUtils.consume(httpResponse.getEntity());
      return newTaskUri;
    } catch (Exception e) {
      throw new RuntimeException("Error creating patavi task: " + e.toString());
    }
  }
  @Override
  public JsonNode getResult(URI taskUri) throws URISyntaxException {
    URI resultsUri = new URIBuilder(taskUri + WebConstants.PATAVI_RESULTS_PATH)
            .build();
    HttpGet getRequest = new HttpGet(resultsUri);
    try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) httpClient.execute(getRequest)) {
      String content = EntityUtils.toString(httpResponse.getEntity());
      EntityUtils.consume(httpResponse.getEntity());
      return objectMapper.readTree(content);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Map<URI, JsonNode> getResults(List<URI> taskUris) throws URISyntaxException {
    List<URI> filteredUris = taskUris.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    Map<URI, JsonNode> result = new HashMap<>();
    for (URI uri : filteredUris) {
      result.put(uri, getResult(uri));
    }
    return result;
  }

  @Override
  public HttpResponse delete(URI taskUrl) {
    try (CloseableHttpResponse httpResponse = (CloseableHttpResponse) httpClient.execute(new HttpDelete(taskUrl))) {
      return httpResponse;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<PataviTask> findByUrls(List<URI> taskUris) throws IOException {
    return taskUris.stream().filter(Objects::nonNull).map(this::getTask).collect(Collectors.toList());
  }

  private PataviTask getTask(URI taskUrl) {
    assert(taskUrl != null);
    logger.trace("getTask for taskURl" + taskUrl.toString());
    try (CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(new HttpGet(taskUrl))) {
      String pataviResponse = EntityUtils.toString(response.getEntity());
      EntityUtils.consume(response.getEntity());
      return new PataviTask(pataviResponse);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}

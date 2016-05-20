package org.drugis.addis.patavitask.repository;

import com.fasterxml.jackson.databind.JsonNode;
import org.json.JSONObject;
import org.apache.http.HttpResponse;
import org.drugis.addis.patavitask.PataviTask;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by connor on 26-6-14.
 */

public interface PataviTaskRepository {

  List<PataviTask> findByUrls(List<URI> taskUris) throws SQLException, IOException;

  JsonNode getResult(URI taskUrl) throws IOException, UnexpectedNumberOfResultsException, URISyntaxException;

  Map<URI, JsonNode> getResults(List<URI> taskUris) throws SQLException, IOException, UnexpectedNumberOfResultsException, URISyntaxException;

  HttpResponse delete(URI taskUrl) throws IOException;

  PataviTask getTask(URI taskUrl) throws IOException;

  URI createPataviTask(URI pataviUri, JSONObject jsonObject);
}

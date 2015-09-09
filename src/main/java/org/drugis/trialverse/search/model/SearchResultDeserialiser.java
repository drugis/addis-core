package org.drugis.trialverse.search.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.drugis.trialverse.util.WebConstants;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by connor on 9/4/15.
 */
public class SearchResultDeserialiser extends JsonDeserializer<List<SearchResult>> {

  private SearchResult createElement(JsonNode jsonNode, String version) {
    String study = jsonNode.get("study").get("value").asText();
    String graphUri = jsonNode.get("graph").get("value").asText();
    String title = jsonNode.get("label").get("value").asText();
    String comment = jsonNode.has("comment") ? jsonNode.get("comment").get("value").asText() : null;
    return new SearchResult(graphUri, study, title, comment, version);
  }

  @Override
  public List<SearchResult> deserialize(JsonParser jp, DeserializationContext ctxt)
          throws IOException {

    JsonNode node = jp.getCodec().readTree(jp);
    String version = node.get(WebConstants.VERSION_UUID).asText();
    Iterator<JsonNode> elements = node.get("results").get("bindings").elements();
    Iterable<JsonNode> iterable = () -> elements;
    return StreamSupport.stream(iterable.spliterator(), true)
            .map((resultNode) -> createElement(resultNode, version))
            .collect(Collectors.toList());
  }
}
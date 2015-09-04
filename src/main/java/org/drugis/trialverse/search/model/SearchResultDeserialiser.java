package org.drugis.trialverse.search.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by connor on 9/4/15.
 */
public class SearchResultDeserialiser extends JsonDeserializer<List<SearchResult>> {

  private SearchResult createElement(JsonNode jsonNode) {
    String study = jsonNode.get("study").get("value").asText();
    String title = jsonNode.get("label").get("value").asText();
    String comment = jsonNode.has("comment") ? jsonNode.get("comment").get("value").asText() : null;
    return new SearchResult(study, title, comment);
  }

  @Override
  public List<SearchResult> deserialize(JsonParser jp, DeserializationContext ctxt)
          throws IOException {

    JsonNode node = jp.getCodec().readTree(jp);
    Iterator<JsonNode> elements = node.get("results").get("bindings").elements();
    Iterable<JsonNode> iterable = () -> elements;
    return StreamSupport.stream(iterable.spliterator(), true)
            .map(this::createElement)
            .collect(Collectors.toList());
  }
}
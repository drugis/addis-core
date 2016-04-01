package org.drugis.addis.interventions.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

/**
 * Created by daan on 1-4-16.
 */
public class BoundTypeDeserializer extends JsonDeserializer<BoundType> {
  @Override
  public BoundType deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
    JsonNode node = jp.getCodec().readTree(jp);
    return BoundType.fromString(node.asText());
  }
}



package org.drugis.addis.util;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

/**
 * Created by connor on 07/04/14.
 */
public class ObjectToStringDeserializer extends JsonDeserializer<String> {
  @Override
  public String deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    ObjectCodec objectCodec = jp.getCodec();
    JsonNode jsonNode = objectCodec.readTree(jp);
    return jsonNode.toString();
  }
}

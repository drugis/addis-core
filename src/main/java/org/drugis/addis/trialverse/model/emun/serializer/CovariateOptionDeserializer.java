package org.drugis.addis.trialverse.model.emun.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.drugis.addis.trialverse.model.emun.CovariateOption;

import java.io.IOException;

/**
 * Created by connor on 12/2/15.
 */
public class CovariateOptionDeserializer extends JsonDeserializer<CovariateOption> {

  @Override
  public CovariateOption deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
    JsonNode node = jp.getCodec().readTree(jp);
    return CovariateOption.fromKey(node.get("key").asText());
  }
}

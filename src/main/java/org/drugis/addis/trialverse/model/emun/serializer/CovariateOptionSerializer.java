package org.drugis.addis.trialverse.model.emun.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.drugis.addis.trialverse.controller.CovariateViewAdapter;
import org.drugis.addis.trialverse.model.emun.CovariateOption;

import java.io.IOException;

/**
 * Created by connor on 12/2/15.
 */
public class CovariateOptionSerializer extends JsonSerializer<CovariateOption> {

  @Override
  public void serialize(CovariateOption value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
    jgen.writeObject(new CovariateViewAdapter(value));
  }
}

package org.drugis.addis.covariates;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * Created by connor on 12/2/15.
 */
public class CovariateOptionSerializer extends JsonSerializer<CovariateOption> {

  @Override
  public void serialize(CovariateOption value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
    jgen.writeObject(new CovariateViewAdepter(value));
  }
}

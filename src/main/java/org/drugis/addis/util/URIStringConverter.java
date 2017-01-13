package org.drugis.addis.util;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.net.URI;

/**
 * Created by joris on 15-12-16.
 */
@Converter
public class URIStringConverter implements AttributeConverter<URI, String> {
  @Override
  public String convertToDatabaseColumn(URI uri) {
    return uri.toString();
  }

  @Override
  public URI convertToEntityAttribute(String s) {
    return URI.create(s);
  }
}

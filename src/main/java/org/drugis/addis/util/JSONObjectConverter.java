package org.drugis.addis.util;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Created by connor on 12/17/15.
 */
@Converter
public class JSONObjectConverter implements AttributeConverter<JSONObject, String> {


  @Override
  public String convertToDatabaseColumn(JSONObject attribute) {
    return attribute == null ? null : attribute.toJSONString();
  }

  @Override
  public JSONObject convertToEntityAttribute(String dbData) {
    if (dbData == null) {
      return null;
    }
    JSONParser jsonParser = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
    try {
      return (JSONObject) jsonParser.parse(dbData);
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }
}

package org.drugis.addis.util;

import net.minidev.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by connor on 12/17/15.
 */
public class JSONObjectConverterTest {

  @Test
  public void testConvertToDatabaseColumn() throws Exception {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("a", "b");
    assertEquals("{\"a\":\"b\"}", new JSONObjectConverter().convertToDatabaseColumn(jsonObject));

  }

  @Test
  public void testConvertToEntityAttribute() throws Exception {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("a", "b");
    assertEquals(jsonObject, new JSONObjectConverter().convertToEntityAttribute("{\"a\":\"b\"}"));
  }
}
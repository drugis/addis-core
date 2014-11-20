package org.drugis.trialverse.testutils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertTrue;


/**
 * Created by connor on 6-11-14.
 */
public class TestUtils {
  public static String createJson(Object o) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.writeValueAsString(o);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static String loadResource(Class clazz, String filename) {
    try {
      InputStream stream = clazz.getResourceAsStream(filename);
      return IOUtils.toString(stream, "UTF-8");
    } catch (IOException e) {
      e.printStackTrace();
      assertTrue(false);
    }
    return "";
  }

}

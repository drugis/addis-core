package org.drugis.addis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.compress.utils.IOUtils;

import java.io.IOException;
import java.io.InputStream;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;

/**
 * Created by connor on 2/12/14.
 */
public class TestUtils {

  public static String createJson(Object o) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.writeValueAsString(o);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return "{}";
  }

  public static String loadResource(Class clazz, String filename) {
    try {
      InputStream stream = clazz.getResourceAsStream(filename);
      return new String(stream.readAllBytes());
    } catch (IOException e) {
      e.printStackTrace();
    }
    throw new RuntimeException("Failed to load class resource");
  }

  public static String buildPataviTaskJson(String id) {
    return "{ \"id\": \"" +
            id +
            "\", \"service\": \"slow\", \"status\": \"done\", \"_links\": { \"self\": { \"href\": \"https://patavi.drugis.org/task/" +
            id +
            "\" }, \"results\": { \"href\": \"https://patavi.drugis.org/task/" +
            id +
            "/results\" }, \"updates\": { \"href\": \"wss://patavi.drugis.org/task/" + id + "/updates\" } } }";
  }
}

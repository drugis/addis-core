package org.drugis.trialverse.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.TestCase.assertNotNull;

/**
 * Created by connor on 4-3-16.
 */
public class UtilTest {

  @Test
  public void testReadMu() throws IOException {

    String muString = "{" +
            "  \"multivariateSummary\": {" +
            "    \"2\": {" +
            "      \"mu\": {" +
            "        \"d.2.3\": 0.30782," +
            "        \"d.2.4\": 0.33512," +
            "        \"d.2.5\": 0.13747" +
            "      }" +
            "    }" +
            "  }" +
            "}";

    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode result = objectMapper.readTree(muString);

    Object mu = Utils.readMu(2, result);
    assertNotNull(mu);
  }
}

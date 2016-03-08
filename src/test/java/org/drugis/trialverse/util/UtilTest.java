package org.drugis.trialverse.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.drugis.addis.problems.model.Baseline;
import org.drugis.addis.problems.model.MultiVariateDistribution;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertNotNull;

/**
 * Created by connor on 4-3-16.
 */
public class UtilTest {

  private ObjectMapper objectMapper = new ObjectMapper();

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

    JsonNode result = objectMapper.readTree(muString);

    Object mu = Utils.readMu(2, result);
    assertNotNull(mu);
  }

  @Test
  public void testParseMultivariateSummary() throws IOException {

    String jsonMultivariateSummary = "{\n" +
            "  \"2\": {\n" +
            "    \"mu\": {\n" +
            "      \"d.2.3\": 0.55302,\n" +
            "      \"d.2.4\": 0.46622,\n" +
            "      \"d.2.5\": 0.34083\n" +
            "    },\n" +
            "    \"sigma\": {\n" +
            "      \"d.2.3\": {\n" +
            "        \"d.2.3\": 74.346,\n" +
            "        \"d.2.4\": 1.9648,\n" +
            "        \"d.2.5\": 1.8133\n" +
            "      },\n" +
            "      \"d.2.4\": {\n" +
            "        \"d.2.3\": 1.9648,\n" +
            "        \"d.2.4\": 74.837,\n" +
            "        \"d.2.5\": 3.1309\n" +
            "      },\n" +
            "      \"d.2.5\": {\n" +
            "        \"d.2.3\": 1.8133,\n" +
            "        \"d.2.4\": 3.1309,\n" +
            "        \"d.2.5\": 74.895\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "  \"3\": {\n" +
            "    \"mu\": {\n" +
            "      \"d.3.2\": -0.55302,\n" +
            "      \"d.3.4\": -0.086804,\n" +
            "      \"d.3.5\": -0.21219\n" +
            "    },\n" +
            "    \"sigma\": {\n" +
            "      \"d.3.2\": {\n" +
            "        \"d.3.2\": 74.346,\n" +
            "        \"d.3.4\": 72.381,\n" +
            "        \"d.3.5\": 72.533\n" +
            "      },\n" +
            "      \"d.3.4\": {\n" +
            "        \"d.3.2\": 72.381,\n" +
            "        \"d.3.4\": 145.25,\n" +
            "        \"d.3.5\": 73.699\n" +
            "      },\n" +
            "      \"d.3.5\": {\n" +
            "        \"d.3.2\": 72.533,\n" +
            "        \"d.3.4\": 73.699,\n" +
            "        \"d.3.5\": 145.62\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "  \"4\": {\n" +
            "    \"mu\": {\n" +
            "      \"d.4.2\": -0.46622,\n" +
            "      \"d.4.3\": 0.086804,\n" +
            "      \"d.4.5\": -0.12539\n" +
            "    },\n" +
            "    \"sigma\": {\n" +
            "      \"d.4.2\": {\n" +
            "        \"d.4.2\": 74.837,\n" +
            "        \"d.4.3\": 72.872,\n" +
            "        \"d.4.5\": 71.706\n" +
            "      },\n" +
            "      \"d.4.3\": {\n" +
            "        \"d.4.2\": 72.872,\n" +
            "        \"d.4.3\": 145.25,\n" +
            "        \"d.4.5\": 71.555\n" +
            "      },\n" +
            "      \"d.4.5\": {\n" +
            "        \"d.4.2\": 71.706,\n" +
            "        \"d.4.3\": 71.555,\n" +
            "        \"d.4.5\": 143.47\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "  \"5\": {\n" +
            "    \"mu\": {\n" +
            "      \"d.5.2\": -0.34083,\n" +
            "      \"d.5.3\": 0.21219,\n" +
            "      \"d.5.4\": 0.12539\n" +
            "    },\n" +
            "    \"sigma\": {\n" +
            "      \"d.5.2\": {\n" +
            "        \"d.5.2\": 74.895,\n" +
            "        \"d.5.3\": 73.082,\n" +
            "        \"d.5.4\": 71.765\n" +
            "      },\n" +
            "      \"d.5.3\": {\n" +
            "        \"d.5.2\": 73.082,\n" +
            "        \"d.5.3\": 145.62,\n" +
            "        \"d.5.4\": 71.916\n" +
            "      },\n" +
            "      \"d.5.4\": {\n" +
            "        \"d.5.2\": 71.765,\n" +
            "        \"d.5.3\": 71.916,\n" +
            "        \"d.5.4\": 143.47\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}\n";

    Map<Integer, MultiVariateDistribution> distributionByInterventionId = objectMapper.readValue(jsonMultivariateSummary,  new TypeReference<Map<Integer, MultiVariateDistribution>>() {});
    assertNotNull(distributionByInterventionId);

    assertEquals(74.346, distributionByInterventionId.get(2).getSigma().get("d.2.3").get("d.2.3") );
  }

  @Test
  public void baselineTest() throws IOException {
    String baselineJsonString = "{\n" +
            "\t      \"scale\": \"log odds\",\n" +
            "\t      \"mu\": 4,\n" +
            "\t      \"sigma\": 6,\n" +
            "\t      \"name\": \"Fluoxetine\"\n" +
            "\t    }";
    Baseline baseline = objectMapper.readValue(baselineJsonString, Baseline.class);
    assertEquals("log odds", baseline.getScale());
    assertEquals(4.0, baseline.getMu());
    assertEquals(6.0, baseline.getSigma());
    assertEquals("Fluoxetine", baseline.getName());
  }
}

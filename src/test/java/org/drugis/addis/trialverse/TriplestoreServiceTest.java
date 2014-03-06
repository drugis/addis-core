package org.drugis.addis.trialverse;

import org.drugis.addis.trialverse.model.SemanticIntervention;
import org.drugis.addis.trialverse.model.SemanticOutcome;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.drugis.addis.trialverse.service.impl.TriplestoreServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Created by connor on 2/28/14.
 */

public class TriplestoreServiceTest {

  @Mock
  RestTemplate triplestoreMock;

  @InjectMocks
  TriplestoreService triplestoreService;

  @Before
  public void setUp() {
    triplestoreService = new TriplestoreServiceImpl();
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testGetOutcomes() {
    String mockResult = buildMockOutcomeResult();
    when(triplestoreMock.getForObject(Mockito.anyString(), Mockito.any(Class.class), Mockito.anyMap())).thenReturn(mockResult);
    List<SemanticOutcome> result = triplestoreService.getOutcomes(1L);
    SemanticOutcome result1 = new SemanticOutcome("http://trials.drugis.org/namespace/1/endpoint/test1", "DBP 24-hour mean");
    assertEquals(result.get(0), result1);
  }

  @Test
  public void testGetInterventions() {
    String mockResult = buildMockInterventionResult();
    when(triplestoreMock.getForObject(Mockito.anyString(), Mockito.any(Class.class), Mockito.anyMap())).thenReturn(mockResult);
    List<SemanticIntervention> result = triplestoreService.getInterventions(1L);
    SemanticIntervention result1 = new SemanticIntervention("http://trials.drugis.org/namespace/1/drug/test1", "Azilsartan");
    assertEquals(result.get(0), result1);
  }

  /*
   * Build json result object containing 2 bindings both with a uri and label
  * */
  private String buildMockOutcomeResult() {
    return "{\n" +
            "      \"results\": {\n" +
            "      \"bindings\": [\n" +
            "      {\n" +
            "        \"uri\": { \"type\": \"uri\" , \"value\": \"http://trials.drugis.org/namespace/1/endpoint/test1\" } ,\n" +
            "        \"label\": { \"type\": \"literal\" , \"value\": \"DBP 24-hour mean\" }\n" +
            "      } ,\n" +
            "      {\n" +
            "        \"uri\": { \"type\": \"uri\" , \"value\": \"http://trials.drugis.org/namespace/1/adverseEvent/test2\" } ,\n" +
            "        \"label\": { \"type\": \"literal\" , \"value\": \"Blood pressure increased\" }\n" +
            "      }]}}";
  }

  /*
 * Build json result object containing 2 bindings both with a uri and label
* */
  private String buildMockInterventionResult() {
    return "{\n" +
            "      \"results\": {\n" +
            "      \"bindings\": [\n" +
            "      {\n" +
            "        \"uri\": { \"type\": \"uri\" , \"value\": \"http://trials.drugis.org/namespace/1/drug/test1\" } ,\n" +
            "        \"label\": { \"type\": \"literal\" , \"value\": \"Azilsartan\" }\n" +
            "      } ,\n" +
            "      {\n" +
            "        \"uri\": { \"type\": \"uri\" , \"value\": \"http://trials.drugis.org/namespace/1/drug/test2\" } ,\n" +
            "        \"label\": { \"type\": \"literal\" , \"value\": \"Placebo\" }\n" +
            "      }]}}";
  }
}

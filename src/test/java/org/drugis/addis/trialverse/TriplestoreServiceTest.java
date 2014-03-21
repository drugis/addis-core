package org.drugis.addis.trialverse;

import org.apache.commons.io.IOUtils;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
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
    String mockResult = loadResource("/triplestoreService/exampleOutcomeResult.json");
    when(triplestoreMock.getForObject(Mockito.anyString(), Mockito.any(Class.class), Mockito.anyMap())).thenReturn(mockResult);
    List<SemanticOutcome> result = triplestoreService.getOutcomes(1L);
    SemanticOutcome result1 = new SemanticOutcome("http://trials.drugis.org/namespace/1/endpoint/test1", "DBP 24-hour mean");
    assertEquals(result.get(0), result1);
  }

  @Test
  public void testGetInterventions() {
    String mockResult = loadResource("/triplestoreService/exampleInterventionResult.json");
    when(triplestoreMock.getForObject(Mockito.anyString(), Mockito.any(Class.class), Mockito.anyMap())).thenReturn(mockResult);
    List<SemanticIntervention> result = triplestoreService.getInterventions(1L);
    SemanticIntervention result1 = new SemanticIntervention("http://trials.drugis.org/namespace/1/drug/test1", "Azilsartan");
    assertEquals(result.get(0), result1);
  }

  private String loadResource(String filename) {
    try {
      InputStream stream = getClass().getResourceAsStream(filename);
      return IOUtils.toString(stream, "UTF-8");
    } catch (IOException e) {
      e.printStackTrace();
      assertTrue(false);
    }
    return "";
  }

}

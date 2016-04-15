package org.drugis.addis.trialverse;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import net.minidev.json.parser.ParseException;
import org.drugis.addis.TestUtils;
import org.drugis.addis.trialverse.model.*;
import org.drugis.addis.trialverse.service.QueryResultMappingService;
import org.drugis.addis.trialverse.service.impl.QueryResultMappingServiceImpl;
import org.drugis.addis.trialverse.service.impl.ReadValueException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by connor on 8-4-16.
 */
public class QueryResultMappingServiceTest {
  @InjectMocks
  QueryResultMappingService queryResultMappingService;

  String resultRows = TestUtils.loadResource(this.getClass(), "/triplestoreService/trialDataEdarbiReultRowsExample.json");
  String covariateRow = TestUtils.loadResource(this.getClass(), "/triplestoreService/covariatePopCharValueRow.json");

  @Before
  public void setUp() {
    queryResultMappingService = new QueryResultMappingServiceImpl();
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testMapResultRowToTrialDataStudy() throws ParseException, ReadValueException, URISyntaxException {
    JSONArray bindings = (JSONArray) JSONValue.parseWithException(resultRows);
    Map<URI, TrialDataStudy> trialDataMap = queryResultMappingService.mapResultRowToTrialDataStudy(bindings);
    assertEquals(5, trialDataMap.size());

    URI studyWithFixedInterventionUri = new URI("http://trials.drugis.org/graphs/294b3fa9-ba49-4c16-a551-afba9b5856a3");
    TrialDataStudy trialDataStudy = trialDataMap.get(studyWithFixedInterventionUri);
    List<TrialDataArm> trialDataArms = trialDataStudy.getTrialDataArms();

    AbstractSemanticIntervention intervention = trialDataArms.get(0).getSemanticIntervention();
    assertTrue(intervention instanceof FixedSemanticIntervention);
    FixedSemanticIntervention fixedSemanticIntervention = (FixedSemanticIntervention) intervention;
    Dose fixedDose = fixedSemanticIntervention.getDose();
    assertEquals("P1D", fixedDose.getPeriodicity());
    assertEquals(new URI("http://trials.drugis.org/concepts/a57c4db5-f4dc-4f4e-93c2-12f02f97ed7b"), fixedDose.getUnitConceptUri());
    assertEquals("milligram", fixedDose.getUnitLabel());
    assertEquals((Double) 0.001d, fixedDose.getUnitMultiplier());
    assertEquals((Double) 40.0d, fixedDose.getValue());


    URI studyWithTitratedInterventionUri = new URI("http://trials.drugis.org/graphs/c600d0ee-9d64-4395-ad06-f4b4843b20f6");
    trialDataStudy = trialDataMap.get(studyWithTitratedInterventionUri);

    intervention= trialDataStudy.getTrialDataArms().get(0).getSemanticIntervention();
    assertTrue(intervention instanceof TitratedSemanticIntervention);
    TitratedSemanticIntervention titratedSemanticIntervention = (TitratedSemanticIntervention) intervention;
    Dose minDose = titratedSemanticIntervention.getMinDose();
    assertEquals("P1D", minDose.getPeriodicity());
    assertEquals("milligram", minDose.getUnitLabel());
    assertEquals((Double) 0.001d, minDose.getUnitMultiplier());
    assertEquals((Double) 20d, minDose.getValue());
    Dose maxDose = titratedSemanticIntervention.getMaxDose();
    assertEquals("P1D", maxDose.getPeriodicity());
    assertEquals("milligram", maxDose.getUnitLabel());
    assertEquals((Double) 0.001d, maxDose.getUnitMultiplier());
    assertEquals((Double) 40.0d, maxDose.getValue());
  }

  @Test
  public void testMapResultToCovariateStudyValue() throws ParseException, ReadValueException {
    JSONObject row  = (JSONObject) JSONValue.parseWithException(covariateRow);
    CovariateStudyValue covariateStudyValue = queryResultMappingService.mapResultToCovariateStudyValue(row);
    assertNotNull(covariateStudyValue);
    assertEquals("uuid", covariateStudyValue.getCovariateKey());
    assertEquals(URI.create("http://its/a/uri"), covariateStudyValue.getStudyUri());
    assertEquals(40d, covariateStudyValue.getValue(), 0.000000001);
  }
}

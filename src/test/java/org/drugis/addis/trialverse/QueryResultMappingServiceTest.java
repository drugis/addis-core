package org.drugis.addis.trialverse;

import com.google.common.collect.Sets;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import net.minidev.json.parser.ParseException;
import org.drugis.addis.TestUtils;
import org.drugis.addis.problems.service.ProblemService;
import org.drugis.addis.trialverse.model.MeasurementMoment;
import org.drugis.addis.trialverse.model.trialdata.*;
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
import java.util.Set;

import static org.junit.Assert.*;

public class QueryResultMappingServiceTest {
  @InjectMocks
  private QueryResultMappingService queryResultMappingService;

  private String covariateRow = TestUtils.loadResource(this.getClass(), "/queryResultMappingService/covariatePopCharValueRow.json");
  private String combinationTreatmentRows = TestUtils.loadResource(this.getClass(), "/queryResultMappingService/trialDataEdarbiCombined.json");
  private String contrastDataRows = TestUtils.loadResource(this.getClass(), "/queryResultMappingService/contrastDataRows.json");

  @Before
  public void setUp() {
    queryResultMappingService = new QueryResultMappingServiceImpl();
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testMapResultRowToTrialDataStudy() throws ReadValueException, URISyntaxException {
    JSONArray bindings = JsonPath.read(combinationTreatmentRows, "$");
    Map<URI, TrialDataStudy> trialDataMap = queryResultMappingService.mapResultRowsToTrialDataStudy(bindings);
    assertEquals(5, trialDataMap.size());

    URI studyWithFixedInterventionUri = new URI("http://trials.drugis.org/graphs/34adef58-434f-40c8-89a3-d93fad6dbd94");
    TrialDataStudy trialDataStudy = trialDataMap.get(studyWithFixedInterventionUri);
    List<TrialDataArm> trialDataArms = trialDataStudy.getArms();

    AbstractSemanticIntervention intervention = trialDataArms.get(0).getSemanticInterventions().get(0);
    assertTrue(intervention instanceof FixedSemanticIntervention);
    FixedSemanticIntervention fixedSemanticIntervention = (FixedSemanticIntervention) intervention;
    Dose fixedDose = fixedSemanticIntervention.getDose();
    assertEquals("P1D", fixedDose.getPeriodicity());
    assertEquals(new URI("http://trials.drugis.org/concepts/dfdd3707-fef5-4f06-b582-85c7de7a101d"), fixedDose.getUnitConceptUri());
    assertEquals("milligram", fixedDose.getUnitLabel());
    assertEquals((Double) 0.001d, fixedDose.getUnitMultiplier());
    assertEquals((Double) 40.0d, fixedDose.getValue());


    URI studyWithTitratedInterventionUri = new URI("http://trials.drugis.org/graphs/27d109cc-3557-4223-97ef-b2cfea99c964");
    trialDataStudy = trialDataMap.get(studyWithTitratedInterventionUri);

    intervention = trialDataStudy.getArms().get(0).getSemanticInterventions().get(0);
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
    assertEquals((Double) 80.0d, maxDose.getValue());
  }

  @Test
  public void testMapContrastRow() throws ReadValueException {
    URI studyUri = URI.create("http://trials.drugis.org/graphs/graph");
    URI variableUri = URI.create("http://trials.drugis.org/instances/variable");
    URI variableConceptUri = URI.create("http://trials.drugis.org/concepts/variable");
    URI armUri = URI.create("http://trials.drugis.org/instances/arm");
    URI referenceArmUri = URI.create("http://trials.drugis.org/instances/referenceArm");
    JSONArray bindings = JsonPath.read(contrastDataRows, "$");

    Map<URI, TrialDataStudy> studies = queryResultMappingService.mapResultRowsToTrialDataStudy(bindings);

    TrialDataStudy study = studies.get(studyUri);
    MeasurementMoment measurementMoment = study.getMeasurementMoments().iterator().next();
    TrialDataArm arm = study.getArms().get(0);
    Set<Measurement> measurements = arm.getMeasurements().get(measurementMoment.getUri());

    Measurement measurement = new MeasurementBuilder(studyUri, variableUri, variableConceptUri, armUri, ProblemService.DICHOTOMOUS_TYPE_URI)
            .setReferenceArm(referenceArmUri)
            .setReferenceStdErr(15.0)
            .setOddsRatio(1.2)
            .setStdErr(14.8)
            .build();
    Set<Measurement> expectedMeasurements = Sets.newHashSet(measurement);

    assertEquals(expectedMeasurements, measurements);
  }


  @Test
  public void testCombinationTreatment() throws ReadValueException {
    JSONArray bindings = JsonPath.read(combinationTreatmentRows, "$");
    Map<URI, TrialDataStudy> trialDataMap = queryResultMappingService.mapResultRowsToTrialDataStudy(bindings);

    final TrialDataStudy combiTreatmentStudy = trialDataMap.get(URI.create("http://trials.drugis.org/graphs/f1d76e55-b04d-4d34-82bd-0e7dd0a8cad0"));

    assertEquals(2, combiTreatmentStudy.getArms().get(0).getSemanticInterventions().size());
  }

  @Test
  public void testMapResultToCovariateStudyValue() throws ParseException, ReadValueException {
    JSONObject row = (JSONObject) JSONValue.parseWithException(covariateRow);
    CovariateStudyValue covariateStudyValue = queryResultMappingService.mapResultToCovariateStudyValue(row);
    assertNotNull(covariateStudyValue);
    assertEquals("http://trials.drugis.org/concepts/uuid", covariateStudyValue.getCovariateKey());
    assertEquals(URI.create("http://its/a/uri"), covariateStudyValue.getStudyUri());
    assertEquals(40d, covariateStudyValue.getValue(), 0.000000001);
  }

}

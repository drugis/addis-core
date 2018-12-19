package org.drugis.addis.problems.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.models.Model;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.problems.model.DataSourceEntry;
import org.drugis.addis.problems.model.NMAInclusionWithResults;
import org.drugis.addis.problems.service.model.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;

import java.io.IOException;
import java.net.URI;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class NetworkBenefitRiskPerformanceEntryBuilderTest {

  @InjectMocks
  private NetworkBenefitRiskPerformanceEntryBuilder networkBenefitRiskPerformanceEntryBuilder;

  @Before
  public void setUp() {
    networkBenefitRiskPerformanceEntryBuilder = new NetworkBenefitRiskPerformanceEntryBuilder();
    initMocks(this);
  }

  @Test
  public void testBuildPerformance() throws IOException {
    Outcome outcome = mock(Outcome.class);
    String outcomeUri = "outcomeUri";
    when(outcome.getConceptOutcomeUri()).thenReturn(URI.create(outcomeUri));
    Model model = mock(Model.class);
    when(model.getLink()).thenReturn(Model.LINK_LOGIT);
    JsonNode pataviResults = buildPataviResults();
    AbstractIntervention fluoxIntervention = buildInterventionMock(11, "fluox");
    AbstractIntervention intervention2 = buildInterventionMock(12, "parox");
    AbstractIntervention intervention3 = buildInterventionMock(13, "sertra");
    Set<AbstractIntervention> interventions = Sets.newHashSet(fluoxIntervention, intervention2, intervention3);
    String baseline ="{\n" +
        "\"type\": \"dnorm\",\n" +
        "\"scale\": \"log odds\",\n" +
        "\"mu\": 4,\n" +
        "\"sigma\": 6,\n" +
        "\"name\": \"fluox\"\n" +
        "}";
    NMAInclusionWithResults inclusion = new NMAInclusionWithResults(outcome, model, pataviResults, interventions, baseline);
    String dataSourceId = "dataSourceId";
    DataSourceEntry dataSource = new DataSourceEntry(dataSourceId, "data source", URI.create("dataSource"));

    // execute
    AbstractMeasurementEntry result = networkBenefitRiskPerformanceEntryBuilder.build(inclusion, dataSource);

    Map<String, Double> mu = new HashMap<>();
    List<String> rowNames = Arrays.asList("11", "12", "13");
    List<Double> dataRow1 = Arrays.asList(0.0, 0.0, 0.0);
    List<Double> dataRow2 = Arrays.asList(0.0, 74.346, 1.9648);
    List<Double> dataRow3 = Arrays.asList(0.0, 1.9648, 74.837);
    List<List<Double>> data = Arrays.asList(dataRow1, dataRow2, dataRow3);
    CovarianceMatrix cov = new CovarianceMatrix(rowNames, rowNames, data);
    Relative expectedRelative = new Relative("dmnorm", mu, cov);
    RelativePerformanceParameters expectedParameters = new RelativePerformanceParameters(baseline, expectedRelative);
    RelativePerformance expectedPerformance = new RelativePerformance("relative-logit-normal", expectedParameters);
    AbstractMeasurementEntry expectedResult = new RelativePerformanceEntry(outcomeUri, dataSourceId, expectedPerformance);

    assertEquals(expectedResult, result);
  }

  private AbstractIntervention buildInterventionMock(int interventionId, String interventionName) {
    AbstractIntervention fluoxIntervention = mock(AbstractIntervention.class);
    when(fluoxIntervention.getId()).thenReturn(interventionId);
    when(fluoxIntervention.getName()).thenReturn(interventionName);
    return fluoxIntervention;
  }

  private JsonNode buildPataviResults() throws IOException {
    ObjectMapper om = new ObjectMapper();
    String results1 = "{\n" +
        "  \"multivariateSummary\": {\n" +
        "    \"11\": {\n" +
        "      \"mu\": {\n" +
        "        \"d.11.12\": 0.55302,\n" +
        "        \"d.11.13\": 0.46622\n" +
        "      },\n" +
        "      \"sigma\": {\n" +
        "        \"d.11.12\": {\n" +
        "          \"d.11.12\": 74.346,\n" +
        "          \"d.11.13\": 1.9648\n" +
        "        },\n" +
        "        \"d.11.13\": {\n" +
        "          \"d.11.12\": 1.9648,\n" +
        "          \"d.11.13\": 74.837\n" +
        "        }\n" +
        "      }\n" +
        "    }\n" +
        "  }\n" +
        "}\n";

    return om.readTree(results1);
  }

}
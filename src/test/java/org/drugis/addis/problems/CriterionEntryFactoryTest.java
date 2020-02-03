package org.drugis.addis.problems;

import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.problems.model.CriterionEntry;
import org.drugis.addis.problems.model.DataSourceEntry;
import org.drugis.addis.problems.model.SingleStudyContext;
import org.drugis.addis.problems.service.impl.CriterionEntryFactory;
import org.drugis.addis.trialverse.model.trialdata.Measurement;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;

import static org.drugis.addis.problems.service.ProblemService.CONTINUOUS_TYPE_URI;
import static org.drugis.addis.problems.service.ProblemService.DICHOTOMOUS_TYPE_URI;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CriterionEntryFactoryTest {


  private URI sourceLink = URI.create("http://www.google.nl");
  private String dataSourceId = "dataSource1";
  private URI variableUri = URI.create("variableUri");
  private String source = "some study";
  private String outcomeId = "outcome id";

  private CriterionEntryFactory criterionEntryFactory;
  private SingleStudyContext context;

  @Before
  public void setUp() {
    criterionEntryFactory = new CriterionEntryFactory();
    context   = new SingleStudyContext();
    Outcome outcome = new Outcome();
    outcome.setName(outcomeId);
    context.setOutcome(outcome);
    context.setDataSourceUuid(dataSourceId);
    context.setSourceLink(sourceLink);
    context.setSource(source);
  }

  @Test
  public void testCreateDichotomous() {
    Measurement measurement = mock(Measurement.class);
    when(measurement.getMeasurementTypeURI()).thenReturn(DICHOTOMOUS_TYPE_URI);
    when(measurement.getVariableUri()).thenReturn(variableUri);

    // Execute
    CriterionEntry criterionEntry = criterionEntryFactory.create(measurement, context);

    // Asserts
    DataSourceEntry dataSourceEntry = new DataSourceEntry(dataSourceId, Arrays.asList(0.0, 1.0), source, sourceLink);
    CriterionEntry expectedEntry = new CriterionEntry(Collections.singletonList(dataSourceEntry), outcomeId, "probability");
    assertEquals(expectedEntry, criterionEntry);
  }

  @Test
  public void testCreateContinuous() {
    Measurement measurement = mock(Measurement.class);
    when(measurement.getMeasurementTypeURI()).thenReturn(CONTINUOUS_TYPE_URI);
    when(measurement.getVariableUri()).thenReturn(variableUri);

    // Execute
    CriterionEntry criterionEntry = criterionEntryFactory.create(measurement, context);

    // Asserts
    DataSourceEntry dataSourceEntry = new DataSourceEntry(dataSourceId, Arrays.asList(null,null), source, sourceLink);
    CriterionEntry expectedEntry = new CriterionEntry(Collections.singletonList(dataSourceEntry), outcomeId, null);
    assertEquals(expectedEntry, criterionEntry);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateUnknown() {
    Measurement measurement = mock(Measurement.class);
    when(measurement.getMeasurementTypeURI()).thenReturn(URI.create("unknown"));
    when(measurement.getVariableUri()).thenReturn(variableUri);

    // Execute
    criterionEntryFactory.create(measurement, context);
  }
}
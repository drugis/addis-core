package org.drugis.addis.problems;

import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.problems.model.CriterionEntry;
import org.drugis.addis.problems.model.DataSourceEntry;
import org.drugis.addis.problems.service.impl.CriterionEntryFactory;
import org.drugis.addis.trialverse.model.SemanticVariable;
import org.drugis.addis.trialverse.model.trialdata.Measurement;
import org.junit.Test;
import org.mockito.Mock;

import java.lang.reflect.Array;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.drugis.addis.problems.service.ProblemService.CONTINUOUS_TYPE_URI;
import static org.drugis.addis.problems.service.ProblemService.DICHOTOMOUS_TYPE_URI;

public class CriterionEntryFactoryTest {


  private URI sourceLink = URI.create("http://www.google.nl");
  private String dataSourceId = "dataSource1";
  private URI variableUri = URI.create("variableUri");

  @Test
  public void testCreateDichotomous() {
    Measurement measurement = mock(Measurement.class);
    when(measurement.getMeasurementTypeURI()).thenReturn(DICHOTOMOUS_TYPE_URI);
    when(measurement.getVariableUri()).thenReturn(variableUri);

    // Execute
    CriterionEntry criterionEntry = CriterionEntryFactory.create(measurement, "outcomeName", dataSourceId, sourceLink);

    // Asserts
    DataSourceEntry dataSourceEntry = new DataSourceEntry(dataSourceId, Arrays.asList(0.0, 1.0), null, "study", sourceLink);
    CriterionEntry expectedEntry = new CriterionEntry(variableUri.toString(), Collections.singletonList(dataSourceEntry), "outcomeName", "probability");
    assertEquals(expectedEntry, criterionEntry);
  }

  @Test
  public void testCreateContinuous() {
    Measurement measurement = mock(Measurement.class);
    when(measurement.getMeasurementTypeURI()).thenReturn(CONTINUOUS_TYPE_URI);
    when(measurement.getVariableUri()).thenReturn(variableUri);

    // Execute
    CriterionEntry criterionEntry = CriterionEntryFactory.create(measurement, "outcomeName", dataSourceId, sourceLink);

    // Asserts
    DataSourceEntry dataSourceEntry = new DataSourceEntry(dataSourceId, Arrays.asList(null,null), null, "study", sourceLink);
    CriterionEntry expectedEntry = new CriterionEntry(variableUri.toString(), Collections.singletonList(dataSourceEntry), "outcomeName", null);
    assertEquals(expectedEntry, criterionEntry);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateUnknown() {
    Measurement measurement = mock(Measurement.class);
    when(measurement.getMeasurementTypeURI()).thenReturn(URI.create("unknown"));
    when(measurement.getVariableUri()).thenReturn(variableUri);

    // Execute
    CriterionEntryFactory.create(measurement, "outcomeName", dataSourceId, sourceLink);
  }
}
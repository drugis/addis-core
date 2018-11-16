package org.drugis.addis.problems;

import org.drugis.addis.problems.model.CriterionEntry;
import org.drugis.addis.problems.model.DataSourceEntry;
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

  private CriterionEntryFactory criterionEntryFactory;

  @Before
  public void setUp() {
    criterionEntryFactory = new CriterionEntryFactory();
  }

  @Test
  public void testCreateDichotomous() {
    Measurement measurement = mock(Measurement.class);
    when(measurement.getMeasurementTypeURI()).thenReturn(DICHOTOMOUS_TYPE_URI);
    when(measurement.getVariableUri()).thenReturn(variableUri);

    // Execute
    CriterionEntry criterionEntry = criterionEntryFactory.create(measurement, "outcomeName", dataSourceId, sourceLink);

    // Asserts
    DataSourceEntry dataSourceEntry = new DataSourceEntry(dataSourceId, Arrays.asList(0.0, 1.0), null, "study", sourceLink);
    CriterionEntry expectedEntry = new CriterionEntry(Collections.singletonList(dataSourceEntry), "outcomeName", "probability");
    assertEquals(expectedEntry, criterionEntry);
  }

  @Test
  public void testCreateContinuous() {
    Measurement measurement = mock(Measurement.class);
    when(measurement.getMeasurementTypeURI()).thenReturn(CONTINUOUS_TYPE_URI);
    when(measurement.getVariableUri()).thenReturn(variableUri);

    // Execute
    CriterionEntry criterionEntry = criterionEntryFactory.create(measurement, "outcomeName", dataSourceId, sourceLink);

    // Asserts
    DataSourceEntry dataSourceEntry = new DataSourceEntry(dataSourceId, Arrays.asList(null,null), null, "study", sourceLink);
    CriterionEntry expectedEntry = new CriterionEntry(Collections.singletonList(dataSourceEntry), "outcomeName", null);
    assertEquals(expectedEntry, criterionEntry);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateUnknown() {
    Measurement measurement = mock(Measurement.class);
    when(measurement.getMeasurementTypeURI()).thenReturn(URI.create("unknown"));
    when(measurement.getVariableUri()).thenReturn(variableUri);

    // Execute
    criterionEntryFactory.create(measurement, "outcomeName", dataSourceId, sourceLink);
  }
}
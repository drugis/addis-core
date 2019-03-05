package org.drugis.addis.problems.service.impl;

import org.drugis.addis.problems.model.CriterionEntry;
import org.drugis.addis.problems.model.DataSourceEntry;
import org.drugis.addis.problems.model.PartialValueFunction;
import org.drugis.addis.trialverse.model.trialdata.Measurement;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.drugis.addis.problems.service.ProblemService.CONTINUOUS_TYPE_URI;
import static org.drugis.addis.problems.service.ProblemService.DICHOTOMOUS_TYPE_URI;
import static org.drugis.addis.problems.service.ProblemService.SURVIVAL_TYPE_URI;

@Service
public class CriterionEntryFactory {
  public CriterionEntry create(Measurement measurement, String outcomeName, String dataSourceId, URI referenceLink)  {
    List<Double> scale;
    String unitOfMeasurement;
    URI measurementTypeURI = measurement.getMeasurementTypeURI();
    if (DICHOTOMOUS_TYPE_URI.equals(measurementTypeURI)) {
      scale = Arrays.asList(0.0, 1.0);
      unitOfMeasurement = "probability";
    } else if (CONTINUOUS_TYPE_URI.equals(measurementTypeURI)) {
      scale = Arrays.asList(null, null);
      unitOfMeasurement = null;
    }else if(SURVIVAL_TYPE_URI.equals(measurementTypeURI)){
      scale = Arrays.asList(null, null);
      unitOfMeasurement = null;
    } else {
      throw new IllegalArgumentException("Unknown measurement type: " + measurementTypeURI);
    }

    PartialValueFunction pvf = null;
    String reference = "study";
    DataSourceEntry dataSourceEntry = new DataSourceEntry(dataSourceId, scale, pvf, reference, referenceLink);
    return new CriterionEntry(
            Collections.singletonList(dataSourceEntry),
            outcomeName,
            unitOfMeasurement);
  }
}

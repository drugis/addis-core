package org.drugis.addis.problems.model;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.drugis.addis.analyses.model.BenefitRiskStudyOutcomeInclusion;
import org.drugis.addis.problems.service.model.AbstractMeasurementEntry;
import org.drugis.addis.util.ObjectToStringDeserializer;
import org.drugis.addis.util.WebConstants;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by daan on 3/21/14.
 */
@JsonDeserialize(using = ObjectToStringDeserializer.class)
public class SingleStudyBenefitRiskProblem extends BenefitRiskProblem {

  public SingleStudyBenefitRiskProblem(Map<String, AlternativeEntry> alternatives, Map<URI, CriterionEntry> criteria, List<AbstractMeasurementEntry> performanceTable) {
    super(WebConstants.SCHEMA_VERSION, criteria, alternatives, performanceTable);
  }

}

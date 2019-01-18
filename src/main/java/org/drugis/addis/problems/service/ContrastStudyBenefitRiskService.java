package org.drugis.addis.problems.service;

import org.drugis.addis.analyses.model.BenefitRiskStudyOutcomeInclusion;
import org.drugis.addis.problems.model.SingleStudyContext;
import org.drugis.addis.problems.service.model.AbstractMeasurementEntry;
import org.drugis.addis.trialverse.model.trialdata.TrialDataArm;

import java.net.URI;
import java.util.List;

public interface ContrastStudyBenefitRiskService {

  List<AbstractMeasurementEntry> buildContrastPerformanceTable(
          List<BenefitRiskStudyOutcomeInclusion> inclusions,
          URI defaultMoment,
          SingleStudyContext context,
          List<TrialDataArm> matchedArms
  );

  List<BenefitRiskStudyOutcomeInclusion> getContrastInclusionsWithBaseline(List<BenefitRiskStudyOutcomeInclusion> benefitRiskStudyOutcomeInclusions, SingleStudyContext context);
}

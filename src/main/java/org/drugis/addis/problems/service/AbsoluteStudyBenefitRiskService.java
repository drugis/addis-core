package org.drugis.addis.problems.service;

import org.drugis.addis.problems.model.SingleStudyContext;
import org.drugis.addis.problems.service.model.AbstractMeasurementEntry;
import org.drugis.addis.trialverse.model.trialdata.TrialDataArm;
import org.drugis.addis.trialverse.model.trialdata.TrialDataStudy;

import java.util.List;

public interface AbsoluteStudyBenefitRiskService {
  List<AbstractMeasurementEntry> buildAbsolutePerformanceEntries(
          SingleStudyContext context,
          TrialDataStudy study,
          List<TrialDataArm> matchedArms);
}

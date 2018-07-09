package org.drugis.addis.problems.service;

import org.drugis.addis.problems.model.AlternativeEntry;
import org.drugis.addis.problems.model.CriterionEntry;
import org.drugis.addis.problems.model.SingleStudyContext;
import org.drugis.addis.problems.model.MeasurementWithCoordinates;
import org.drugis.addis.problems.service.model.AbstractMeasurementEntry;
import org.drugis.addis.projects.Project;
import org.drugis.addis.trialverse.model.trialdata.TrialDataArm;
import org.drugis.addis.trialverse.model.trialdata.TrialDataStudy;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SingleStudyBenefitRiskService {
  List<AbstractMeasurementEntry> buildPerformanceTable(Set<MeasurementWithCoordinates> measurementDrugInstancePairs);

  Set<MeasurementWithCoordinates> getMeasurementsWithCoordinates(List<TrialDataArm> armsWithMatching, URI defaultMeasurementMoment, SingleStudyContext context);

  TrialDataStudy getSingleStudyMeasurements(Project project, URI studyGraphUri, SingleStudyContext context);

  Map<URI,CriterionEntry> getCriteria(List<TrialDataArm> armsWithMatching, URI defaultMeasurementMoment, SingleStudyContext context);

  Map<String,AlternativeEntry> getAlternatives(List<TrialDataArm> armsWithMatching, SingleStudyContext context);
}

package org.drugis.addis.problems.service;

import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.problems.model.AlternativeEntry;
import org.drugis.addis.problems.model.CriterionEntry;
import org.drugis.addis.problems.model.SingleStudyContext;
import org.drugis.addis.problems.service.model.AbstractMeasurementEntry;
import org.drugis.addis.projects.Project;
import org.drugis.addis.trialverse.model.trialdata.TrialDataArm;
import org.drugis.addis.trialverse.model.trialdata.TrialDataStudy;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SingleStudyBenefitRiskService {
  List<AbstractMeasurementEntry> buildPerformanceTable(SingleStudyContext context, TrialDataStudy study, Set<AbstractIntervention> includedInterventions);

  TrialDataStudy getStudy(Project project, URI studyGraphUri, SingleStudyContext context);

  Map<URI, CriterionEntry> getCriteria(List<TrialDataArm> armsWithMatching, URI defaultMeasurementMoment, SingleStudyContext context);

  Map<String, AlternativeEntry> getAlternatives(List<TrialDataArm> armsWithMatching, SingleStudyContext context);

  List<TrialDataArm> getMatchedArms(Set<AbstractIntervention> includedInterventions, List<TrialDataArm> arms);

  SingleStudyContext buildContext(Project project, URI studyGraphUri, Set<Outcome> outcomes, Set<AbstractIntervention> includedInterventions);

  List<AbstractMeasurementEntry> buildContrastPerformanceTable();
}

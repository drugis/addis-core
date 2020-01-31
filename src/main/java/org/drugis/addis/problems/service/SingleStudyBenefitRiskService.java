package org.drugis.addis.problems.service;

import org.drugis.addis.analyses.model.BenefitRiskStudyOutcomeInclusion;
import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.problems.model.*;
import org.drugis.addis.projects.Project;
import org.drugis.addis.trialverse.model.trialdata.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SingleStudyBenefitRiskService {
  SingleStudyBenefitRiskProblem getSingleStudyBenefitRiskProblem(
          Project project,
          BenefitRiskStudyOutcomeInclusion outcomeInclusion,
          Outcome outcome,
          Set<AbstractIntervention> includedInterventions,
          String source);

  TrialDataStudy getStudy(Project project, URI studyGraphUri, SingleStudyContext context);

  Map<URI, CriterionEntry> getCriteria(List<TrialDataArm> armsWithMatching, URI defaultMeasurementMoment, SingleStudyContext context);

  Map<String, AlternativeEntry> getAlternatives(List<TrialDataArm> armsWithMatching, SingleStudyContext context);

  List<TrialDataArm> getMatchedArms(Set<AbstractIntervention> includedInterventions, List<TrialDataArm> arms);

  SingleStudyContext buildContext(
          Project project,
          URI studyGraphUri,
          Outcome outcome,
          Set<AbstractIntervention> includedInterventions,
          BenefitRiskStudyOutcomeInclusion inclusion,
          String source);
}

package org.drugis.addis.problems.service.impl;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.tuple.Pair;
import org.drugis.addis.analyses.model.BenefitRiskStudyOutcomeInclusion;
import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.interventions.model.SingleIntervention;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.problems.model.*;
import org.drugis.addis.problems.service.*;
import org.drugis.addis.problems.service.model.*;
import org.drugis.addis.projects.Project;
import org.drugis.addis.trialverse.model.trialdata.Measurement;
import org.drugis.addis.trialverse.model.trialdata.TrialDataArm;
import org.drugis.addis.trialverse.model.trialdata.TrialDataStudy;
import org.drugis.addis.trialverse.service.MappingService;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.drugis.addis.trialverse.service.impl.ReadValueException;
import org.drugis.trialverse.util.service.UuidService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@Service
public class SingleStudyBenefitRiskServiceImpl implements SingleStudyBenefitRiskService {

  @Inject
  private CriterionEntryFactory criterionEntryFactory;

  @Inject
  private TriplestoreService triplestoreService;

  @Inject
  private MappingService mappingService;

  @Inject
  private AnalysisService analysisService;

  @Inject
  private UuidService uuidService;

  @Inject
  private LinkService linkService;

  @Inject
  private ContrastStudyBenefitRiskService contrastStudyBenefitRiskService;

  @Inject
  private AbsoluteStudyBenefitRiskService absoluteStudyBenefitRiskService;

  @Override
  public SingleStudyBenefitRiskProblem getSingleStudyBenefitRiskProblem(
          Project project,
          List<BenefitRiskStudyOutcomeInclusion> outcomeInclusions,
          URI studyGraphUri,
          Map<Integer, Outcome> outcomesById,
          Set<AbstractIntervention> includedInterventions
  ) {
    SingleStudyContext context = buildContext(project, studyGraphUri, outcomesById, includedInterventions);
    TrialDataStudy study = getStudy(project, studyGraphUri, context);

    List<TrialDataArm> matchedArms = getMatchedArms(includedInterventions, study.getArms());
    URI defaultMeasurementMoment = study.getDefaultMeasurementMoment();

    Map<URI, CriterionEntry> criteria =
            getCriteria(matchedArms, defaultMeasurementMoment, context, outcomeInclusions);
    Map<String, AlternativeEntry> alternatives =
            getAlternatives(matchedArms, context);

    List<AbstractMeasurementEntry> allEntries = new ArrayList<>();
    List<AbstractMeasurementEntry> absoluteEntries =
            absoluteStudyBenefitRiskService.buildAbsolutePerformanceEntries(
                    context,
                    study,
                    matchedArms);
    List<AbstractMeasurementEntry> contrastEntries =
            contrastStudyBenefitRiskService.buildContrastPerformanceTable(
                    outcomeInclusions,
                    defaultMeasurementMoment,
                    context,
                    matchedArms);
    allEntries.addAll(absoluteEntries);
    allEntries.addAll(contrastEntries);

    return new SingleStudyBenefitRiskProblem(alternatives, criteria, allEntries);
  }

  @Override
  public TrialDataStudy getStudy(Project project, URI studyGraphUri, SingleStudyContext context) {
    Set<AbstractIntervention> interventions = ImmutableSet.copyOf(context.getInterventionsById().values());
    final Set<URI> interventionUris = getSingleInterventionUris(interventions);
    final String versionedUuid = mappingService.getVersionedUuid(project.getNamespaceUid());
    try {
      List<TrialDataStudy> studies = triplestoreService.getSingleStudyData(versionedUuid,
              studyGraphUri, project.getDatasetVersion(), context.getOutcomesByUri().keySet(), interventionUris);
      return studies.iterator().next();
    } catch (ReadValueException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Set<URI> getSingleInterventionUris(Set<AbstractIntervention> includedInterventions) {
    try {
      Set<SingleIntervention> singleIncludedInterventions = analysisService.getSingleInterventions(includedInterventions);
      return singleIncludedInterventions.stream()
              .map(SingleIntervention::getSemanticInterventionUri)
              .collect(toSet());
    } catch (ResourceDoesNotExistException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Map<URI, CriterionEntry> getCriteria(
          List<TrialDataArm> arms,
          URI defaultMeasurementMoment, SingleStudyContext context,
          List<BenefitRiskStudyOutcomeInclusion> inclusions
  ) {
    Map<URI, CriterionEntry> criteria = new HashMap<>();
    for (TrialDataArm arm : arms) {
      Set<Measurement> measurements = arm.getMeasurementsForMoment(defaultMeasurementMoment);
      criteria.putAll(getCriterionEntries(measurements, context, inclusions));
    }
    return criteria;
  }

  private Map<URI, CriterionEntry> getCriterionEntries(
          Set<Measurement> measurements,
          SingleStudyContext context,
          List<BenefitRiskStudyOutcomeInclusion> inclusions
  ) {
    return measurements
            .stream()
            .map(measurement -> {
              Outcome outcome = context.getOutcome(measurement.getVariableConceptUri());
              if (isContrastMeasurement(measurement)) {
                BenefitRiskStudyOutcomeInclusion inclusion = getInclusion(inclusions, outcome.getId());
                if (hasMissingBaseline(inclusion)) {
                  return null;
                }
              }
              String dataSourceId = context.getDataSourceId(outcome.getSemanticOutcomeUri());
              CriterionEntry criterionEntry = criterionEntryFactory.create(measurement, outcome.getName(), dataSourceId, context.getSourceLink());
              return Pair.of(measurement.getVariableConceptUri(), criterionEntry);
            })
            .filter(Objects::nonNull)
            .collect(toMap(Pair::getLeft, Pair::getRight));
  }

  private boolean hasMissingBaseline(BenefitRiskStudyOutcomeInclusion inclusion) {
    return inclusion.getBaseline() == null;
  }

  private boolean isContrastMeasurement(Measurement measurement) {
    return measurement.getReferenceArm() != null;
  }

  private BenefitRiskStudyOutcomeInclusion getInclusion(
          List<BenefitRiskStudyOutcomeInclusion> inclusions,
          Integer outcomeId
  ) {
    for (BenefitRiskStudyOutcomeInclusion inclusion : inclusions) {
      if (inclusion.getOutcomeId().equals(outcomeId)) {
        return inclusion;
      }
    }
    return null;
  }

  @Override
  public Map<String, AlternativeEntry> getAlternatives(List<TrialDataArm> armsWithMatching,
                                                       SingleStudyContext context) {
    Map<String, AlternativeEntry> alternatives = new HashMap<>();
    for (TrialDataArm arm : armsWithMatching) {
      Integer matchedProjectInterventionId = arm.getMatchedProjectInterventionIds().iterator().next();
      AbstractIntervention intervention = context.getInterventionsById().get(matchedProjectInterventionId);
      alternatives.put(intervention.getId().toString(), new AlternativeEntry(matchedProjectInterventionId, intervention.getName()));
    }
    return alternatives;
  }


  @Override
  public List<TrialDataArm> getMatchedArms(Set<AbstractIntervention> includedInterventions,
                                           List<TrialDataArm> arms) {
    List<TrialDataArm> armsWithMatching = getArmsWithMatching(includedInterventions, arms);

    Boolean isArmWithTooManyMatches = armsWithMatching.stream()
            .anyMatch(arm -> arm.getMatchedProjectInterventionIds().size() > 1);
    if (isArmWithTooManyMatches) {
      throw new RuntimeException("too many matched interventions for arm when creating problem");
    }
    return armsWithMatching;
  }

  private List<TrialDataArm> getArmsWithMatching(Set<AbstractIntervention> includedInterventions, List<TrialDataArm> arms) {
    return arms.stream()
            .peek(arm -> {
              Set<AbstractIntervention> matchingInterventions =
                      triplestoreService.findMatchingIncludedInterventions(includedInterventions, arm);
              Set<Integer> matchedInterventionIds = matchingInterventions.stream()
                      .map(AbstractIntervention::getId)
                      .collect(toSet());
              arm.setMatchedProjectInterventionIds(ImmutableSet.copyOf(matchedInterventionIds));
            })
            .filter(arm -> arm.getMatchedProjectInterventionIds().size() != 0)
            .collect(Collectors.toList());
  }

  @Override
  public SingleStudyContext buildContext(Project project, URI studyGraphUri, Map<Integer, Outcome> outcomesById, Set<AbstractIntervention> includedInterventions) {
    Map<URI, String> dataSourceIdsByOutcomeUri = outcomesById.values().stream()
            .collect(Collectors.toMap(Outcome::getSemanticOutcomeUri, o -> uuidService.generate()));
    final Map<Integer, AbstractIntervention> interventionsById = includedInterventions.stream()
            .collect(toMap(AbstractIntervention::getId, identity()));
    Map<URI, Outcome> outcomesByUri = outcomesById.values().stream().collect(toMap(Outcome::getSemanticOutcomeUri, identity()));
    URI sourceLink = linkService.getStudySourceLink(project, studyGraphUri);

    SingleStudyContext context = new SingleStudyContext();
    context.setDataSourceIdsByOutcomeUri(dataSourceIdsByOutcomeUri);
    context.setInterventionsById(interventionsById);
    context.setOutcomesByUri(outcomesByUri);
    context.setOutcomesById(outcomesById);
    context.setSourceLink(sourceLink);

    return context;
  }
}

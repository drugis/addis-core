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
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.util.service.UuidService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
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
          BenefitRiskStudyOutcomeInclusion inclusion,
          Outcome outcome,
          Set<AbstractIntervention> includedInterventions,
          String source) {
    URI studyGraphUri = inclusion.getStudyGraphUri();
    SingleStudyContext context = buildContext(project, studyGraphUri, outcome, includedInterventions, inclusion, source);
    TrialDataStudy study = getStudy(project, studyGraphUri, context);

    List<TrialDataArm> matchedArms = getMatchedArms(includedInterventions, study.getArms());
    URI defaultMeasurementMoment = study.getDefaultMeasurementMoment();

    Map<URI, CriterionEntry> criteria =
            getCriteria(matchedArms, defaultMeasurementMoment, context);
    Map<String, AlternativeEntry> alternatives =
            getAlternatives(matchedArms, context);

    List<AbstractMeasurementEntry> allEntries = new ArrayList<>();
    List<AbstractMeasurementEntry> absoluteEntries = absoluteStudyBenefitRiskService.buildAbsolutePerformanceEntries(
            context,
            study,
            matchedArms);
    List<AbstractMeasurementEntry> contrastEntries = contrastStudyBenefitRiskService.buildContrastPerformanceTable(
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
      Set<URI> outcomeUriSet = new HashSet<>();
      outcomeUriSet.add(context.getOutcome().getSemanticOutcomeUri());
      List<TrialDataStudy> studies = triplestoreService.getSingleStudyData(versionedUuid,
              studyGraphUri, project.getDatasetVersion(), outcomeUriSet, interventionUris);
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

  public Map<URI, CriterionEntry> getCriteria(
          List<TrialDataArm> arms,
          URI defaultMeasurementMoment,
          SingleStudyContext context
  ) {
    Map<URI, CriterionEntry> criteria = new HashMap<>();
    for (TrialDataArm arm : arms) {
      Set<Measurement> measurements = arm.getMeasurementsForMoment(defaultMeasurementMoment);
      criteria.putAll(getCriterionEntries(measurements, context));
    }
    return criteria;
  }

  private Map<URI, CriterionEntry> getCriterionEntries(
          Set<Measurement> measurements,
          SingleStudyContext context
  ) {
    return measurements
            .stream()
            .map(measurement -> {
              Outcome outcome = context.getOutcome();
              if (isContrastMeasurementForOutcome(measurement, outcome.getSemanticOutcomeUri())) {
                BenefitRiskStudyOutcomeInclusion inclusion = context.getInclusion();
                if (hasMissingBaseline(inclusion)) {
                  return null;
                }
              }
              CriterionEntry criterionEntry = criterionEntryFactory.create(measurement, context);
              return Pair.of(measurement.getVariableConceptUri(), criterionEntry);
            })
            .filter(Objects::nonNull)
            .collect(toMap(Pair::getLeft, Pair::getRight));
  }

  private boolean hasMissingBaseline(BenefitRiskStudyOutcomeInclusion inclusion) {
    return inclusion.getBaseline() == null;
  }

  private boolean isContrastMeasurementForOutcome(Measurement measurement, URI outcomeUri) {
    return measurement.getVariableConceptUri().equals(outcomeUri) && measurement.getReferenceArm() != null;
  }

  @Override
  public Map<String, AlternativeEntry> getAlternatives(List<TrialDataArm> armsWithMatching,
                                                       SingleStudyContext context) {
    Map<String, AlternativeEntry> alternatives = new HashMap<>();
    for (TrialDataArm arm : armsWithMatching) {
      Integer matchedProjectInterventionId = arm.getMatchedProjectInterventionIds().iterator().next();
      AbstractIntervention intervention = context.getInterventionsById().get(matchedProjectInterventionId);
      alternatives.put(intervention.getId().toString(), new AlternativeEntry(intervention.getName()));
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
  public SingleStudyContext buildContext(
          Project project,
          URI studyGraphUri,
          Outcome outcome,
          Set<AbstractIntervention> includedInterventions,
          BenefitRiskStudyOutcomeInclusion inclusion, String source) {
    String dataSourceUuid = uuidService.generate();
    final Map<Integer, AbstractIntervention> interventionsById = includedInterventions.stream()
            .collect(toMap(AbstractIntervention::getId, identity()));
    URI sourceLink = linkService.getStudySourceLink(project, studyGraphUri);
    SingleStudyContext context = new SingleStudyContext();
    context.setInterventionsById(interventionsById);
    context.setSource(source);
    context.setSourceLink(sourceLink);
    context.setDataSourceUuid(dataSourceUuid);
    context.setOutcome(outcome);
    context.setInclusion(inclusion);
    return context;
  }
}

package org.drugis.addis.problems.service.impl;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.tuple.Pair;
import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.interventions.model.SingleIntervention;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.problems.model.*;
import org.drugis.addis.problems.service.LinkService;
import org.drugis.addis.problems.service.ProblemService;
import org.drugis.addis.problems.service.SingleStudyBenefitRiskService;
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

/**
 * Created by daan on 3/27/14.
 */
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

  @Override
  public List<AbstractMeasurementEntry> buildPerformanceTable(
          SingleStudyContext context,
          TrialDataStudy study,
          Set<AbstractIntervention> includedInterventions) {
    List<TrialDataArm> matchedArms = getMatchedArms(includedInterventions, study.getArms());
    URI defaultMoment = study.getDefaultMeasurementMoment();
    Set<MeasurementWithCoordinates> measurementDrugInstancePairs = getMeasurementsWithCoordinates(
            matchedArms, defaultMoment, context);

    ArrayList<AbstractMeasurementEntry> performanceTable = new ArrayList<>();
    Set<MeasurementWithCoordinates> absoluteMeasurements = filterAbsoluteMeasurements(measurementDrugInstancePairs);
    Set<MeasurementWithCoordinates> contrastMeasurements = filterContrastMeasurements(measurementDrugInstancePairs);
    addAbsolutePerformanceEntries(performanceTable, absoluteMeasurements);
    addContrastPerformanceEntries(performanceTable, study, matchedArms, context, contrastMeasurements);
    return performanceTable;
  }

  private void addContrastPerformanceEntries(
          ArrayList<AbstractMeasurementEntry> performanceTable,
          TrialDataStudy study,
          List<TrialDataArm> arms,
          SingleStudyContext context,
          Set<MeasurementWithCoordinates> contrastMeasurements
  ) {
    URI defaultMoment = study.getDefaultMeasurementMoment();
    List<AbstractMeasurementEntry> contrastEntries = arms.stream().map(
            arm -> createContrastPerformanceEntry(arm, defaultMoment, context, contrastMeasurements)).collect(Collectors.toList());
    performanceTable.addAll(contrastEntries);
  }

  private AbstractMeasurementEntry createContrastPerformanceEntry(
          TrialDataArm arm,
          URI defaultMoment,
          SingleStudyContext context,
          Set<MeasurementWithCoordinates> contrastMeasurements
  ) {
    Set<Measurement> measurementsForMoment = arm.getMeasurementsForMoment(defaultMoment);
    Measurement firstMeasurement = measurementsForMoment.iterator().next();
    String baselineUri = firstMeasurement.getReferenceArm().toString();

    String baseline = getBaseline();
    Map<String, Double> mu = getMu(contrastMeasurements);
    List<String> rowIds = getRowIds(contrastMeasurements);
    List<List<Double>> data = createCovData(contrastMeasurements, rowIds);
    CovarianceMatrix cov = new CovarianceMatrix(rowIds, rowIds, data);

    Relative relative = new Relative(getMeasurementType(firstMeasurement), mu, cov);
    RelativePerformanceParameters parameter = new RelativePerformanceParameters(baselineUri, relative);
    RelativePerformance performance = new RelativePerformance("dmnorm", parameter);

    String criterionUri = firstMeasurement.getVariableConceptUri().toString();//klopt
    String dataSourceId = context.getDataSourceIdsByOutcomeUri().get(firstMeasurement.getVariableConceptUri());
    return new RelativePerformanceEntry(criterionUri, dataSourceId, performance);
  }

  private List<List<Double>> createCovData(
          Set<MeasurementWithCoordinates> contrastMeasurements,
          List<String> rowIds
  ) {
    return null;
  }

  private String getBaseline() {
    return "";
  }


  private List<String> getRowIds(Set<MeasurementWithCoordinates> measurements) {
    return measurements.stream().map(
            measurement -> measurement.getInterventionId().toString())
            .collect(Collectors.toList());
  }

  private Map<String, Double> getMu(Set<MeasurementWithCoordinates> measurements) {
    Map<String, Double> mu = new HashMap<>();
    measurements.forEach(measurement ->
            mu.put(measurement.getInterventionId().toString(), getValue(measurement.getMeasurement())));
    return mu;
  }

  private Double getValue(Measurement measurement) {
    if (measurement.getOddsRatio() != null) {
      return measurement.getOddsRatio();
    }
    if (measurement.getHazardRatio() != null) {
      return measurement.getHazardRatio();
    }
    if (measurement.getRiskRatio() != null) {
      return measurement.getRiskRatio();
    }
    if (measurement.getMeanDifference() != null) {
      return measurement.getMeanDifference();
    }
    if (measurement.getStandardizedMeanDifference() != null) {
      return measurement.getStandardizedMeanDifference();
    }
    return 0.0;//baseline
  }


  private String getMeasurementType(Measurement measurement) {
    if (measurement.getOddsRatio() != null) {
      return "relative-logit-normal";
    }
    if (measurement.getHazardRatio() != null) {
      return "relative-survival";
    }
    if (measurement.getRiskRatio() != null) {
      return "relative-log-normal";
    }
    if (measurement.getMeanDifference() != null) {
      return "relative-normal";
    }
    if (measurement.getStandardizedMeanDifference() != null) {
      return "relative-smd-normal";
    }
    return "relative-normal";
  }

  private void addAbsolutePerformanceEntries(ArrayList<AbstractMeasurementEntry> performanceTable, Set<MeasurementWithCoordinates> absoluteMeasurements) {
    for (MeasurementWithCoordinates measurementWithCoordinates : absoluteMeasurements) {
      Measurement measurement = measurementWithCoordinates.getMeasurement();
      Integer interventionId = measurementWithCoordinates.getInterventionId();
      String dataSource = measurementWithCoordinates.getDataSource();
      if (measurement.getMeasurementTypeURI().equals(ProblemService.DICHOTOMOUS_TYPE_URI)) {
        performanceTable.add(createBetaDistributionEntry(interventionId, measurement.getVariableConceptUri(), dataSource,
                measurement.getRate(), measurement.getSampleSize()));
      } else if (measurement.getMeasurementTypeURI().equals(ProblemService.CONTINUOUS_TYPE_URI)) {
        performanceTable.add(createNormalDistributionEntry(interventionId, measurement.getVariableConceptUri(), dataSource,
                measurement.getMean(), measurement.getStdDev(), measurement.getSampleSize(), measurement.getStdErr()));
      } else {
        throw new IllegalArgumentException("Unknown measurement type: " + measurement.getMeasurementTypeURI());
      }
    }
  }

  private Set<MeasurementWithCoordinates> filterContrastMeasurements(Set<MeasurementWithCoordinates> measurementDrugInstancePairs) {
    return measurementDrugInstancePairs.stream().filter(measurement -> measurement.getMeasurement().getReferenceArm() != null).collect(Collectors.toSet());
  }

  private Set<MeasurementWithCoordinates> filterAbsoluteMeasurements(Set<MeasurementWithCoordinates> measurementDrugInstancePairs) {
    return measurementDrugInstancePairs.stream().filter(measurement -> measurement.getMeasurement().getReferenceArm() == null).collect(Collectors.toSet());
  }

  private ContinuousMeasurementEntry createNormalDistributionEntry(Integer interventionId, URI criterionUri, String dataSourceUri, Double mean, Double standardDeviation, Integer sampleSize, Double standardError) {
    Double sigma = standardError != null ? standardError : standardDeviation / Math.sqrt(sampleSize);

    ContinuousPerformance performance = new ContinuousPerformance(new ContinuousPerformanceParameters(mean, sigma));
    return new ContinuousMeasurementEntry(interventionId, criterionUri.toString(), dataSourceUri, performance);
  }

  private RateMeasurementEntry createBetaDistributionEntry(Integer interventionId, URI criterionUri, String dataSourceUri, Integer rate, Integer sampleSize) {
    int alpha = rate + 1;
    int beta = sampleSize - rate + 1;

    RatePerformance performance = new RatePerformance(new RatePerformanceParameters(alpha, beta));
    return new RateMeasurementEntry(interventionId, criterionUri.toString(), dataSourceUri, performance);
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
  public Map<URI, CriterionEntry> getCriteria(List<TrialDataArm> arms,
                                              URI defaultMeasurementMoment, SingleStudyContext context) {
    Map<URI, CriterionEntry> criteria = new HashMap<>();
    for (TrialDataArm arm : arms) {
      Set<Measurement> measurements = arm.getMeasurementsForMoment(defaultMeasurementMoment);
      criteria.putAll(getCriterionEntries(measurements, context));
    }
    return criteria;
  }

  private Map<URI, CriterionEntry> getCriterionEntries(Set<Measurement> measurements, SingleStudyContext context) {
    return measurements.stream()
            .map(measurement -> {
              Outcome measuredOutcome = context.getOutcomesByUri().get(measurement.getVariableConceptUri());
              String dataSourceId = context.getDataSourceIdsByOutcomeUri().get(measuredOutcome.getConceptOutcomeUri());
              CriterionEntry criterionEntry = criterionEntryFactory.create(measurement, measuredOutcome.getName(), dataSourceId, context.getSourceLink());
              return Pair.of(measurement.getVariableConceptUri(), criterionEntry);
            })
            .collect(toMap(Pair::getLeft, Pair::getRight));
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

  private Set<MeasurementWithCoordinates> getMeasurementsWithCoordinates(List<TrialDataArm> arms, URI defaultMeasurementMoment, SingleStudyContext context) {
    Set<MeasurementWithCoordinates> measurementsWithCoordinates = new HashSet<>();
    for (TrialDataArm arm : arms) {
      Set<Measurement> measurements = arm.getMeasurementsForMoment(defaultMeasurementMoment);
      Integer interventionId = arm.getMatchedProjectInterventionIds().iterator().next();

      Collection<MeasurementWithCoordinates> armMeasurements = getMeasurementEntries(measurements, interventionId, context);

      measurementsWithCoordinates.addAll(armMeasurements);
    }
    return measurementsWithCoordinates;
  }

  private Collection<MeasurementWithCoordinates> getMeasurementEntries(Set<Measurement> measurements, Integer interventionId, SingleStudyContext context) {
    return measurements.stream()
            .map(measurement -> getMeasurementWithCoordinates(measurement, context, interventionId))
            .collect(toList());
  }

  private MeasurementWithCoordinates getMeasurementWithCoordinates(Measurement measurement, SingleStudyContext context, Integer interventionId) {
    Outcome measuredOutcome = context.getOutcomesByUri().get(measurement.getVariableConceptUri());
    String dataSourceId = context.getDataSourceIdsByOutcomeUri().get(measuredOutcome.getConceptOutcomeUri());
    return new MeasurementWithCoordinates(measurement, interventionId, dataSourceId, measuredOutcome.getConceptOutcomeUri());
  }

  @Override
  public List<TrialDataArm> getMatchedArms(Set<AbstractIntervention> includedInterventions, List<TrialDataArm> arms) {
    List<TrialDataArm> armsWithMatching = arms.stream()
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
    Boolean isArmWithTooManyMatches = armsWithMatching.stream()
            .anyMatch(arm -> arm.getMatchedProjectInterventionIds().size() > 1);
    if (isArmWithTooManyMatches) {
      throw new RuntimeException("too many matched interventions for arm when creating problem");
    }
    return armsWithMatching;
  }

  @Override
  public SingleStudyContext buildContext(Project project, URI studyGraphUri, Set<Outcome> outcomes, Set<AbstractIntervention> includedInterventions) {
    Map<URI, String> dataSourceIdsByOutcomeUri = outcomes.stream()
            .collect(Collectors.toMap(Outcome::getConceptOutcomeUri, o -> uuidService.generate()));
    final Map<Integer, AbstractIntervention> interventionsById = includedInterventions.stream()
            .collect(toMap(AbstractIntervention::getId, identity()));
    Map<URI, Outcome> outcomesByUri = outcomes.stream().collect(toMap(Outcome::getConceptOutcomeUri, identity()));
    URI sourceLink = linkService.getStudySourceLink(project, studyGraphUri);

    return new SingleStudyContext(outcomesByUri, interventionsById, dataSourceIdsByOutcomeUri, sourceLink);
  }
}

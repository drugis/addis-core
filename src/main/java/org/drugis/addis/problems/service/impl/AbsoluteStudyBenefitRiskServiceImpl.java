package org.drugis.addis.problems.service.impl;

import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.problems.model.MeasurementWithCoordinates;
import org.drugis.addis.problems.model.SingleStudyContext;
import org.drugis.addis.problems.service.AbsoluteStudyBenefitRiskService;
import org.drugis.addis.problems.service.ProblemService;
import org.drugis.addis.problems.service.model.*;
import org.drugis.addis.trialverse.model.trialdata.Measurement;
import org.drugis.addis.trialverse.model.trialdata.TrialDataArm;
import org.drugis.addis.trialverse.model.trialdata.TrialDataStudy;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
public class AbsoluteStudyBenefitRiskServiceImpl implements AbsoluteStudyBenefitRiskService {

  @Override
  public List<AbstractMeasurementEntry> buildAbsolutePerformanceEntries(
          SingleStudyContext context,
          TrialDataStudy study,
          List<TrialDataArm> matchedArms) {
    URI defaultMoment = study.getDefaultMeasurementMoment();
    Set<MeasurementWithCoordinates> measurementsWithCoordinates = getMeasurementsWithCoordinates(
            matchedArms, defaultMoment, context);
    Set<MeasurementWithCoordinates> absoluteMeasurements = filterAbsoluteMeasurements(measurementsWithCoordinates);
    return createAbsolutePerformanceEntries(absoluteMeasurements);
  }

  private Set<MeasurementWithCoordinates> filterAbsoluteMeasurements(Set<MeasurementWithCoordinates> measurementDrugInstancePairs) {
    return measurementDrugInstancePairs.stream().filter(measurement -> measurement.getMeasurement().getReferenceArm() == null).collect(Collectors.toSet());
  }

  private ArrayList<AbstractMeasurementEntry> createAbsolutePerformanceEntries(
          Set<MeasurementWithCoordinates> absoluteMeasurements) {
    ArrayList<AbstractMeasurementEntry> performanceTable = new ArrayList<>();
    for (MeasurementWithCoordinates measurementWithCoordinates : absoluteMeasurements) {
      AbstractMeasurementEntry entry = createEntry(measurementWithCoordinates);
      performanceTable.add(entry);
    }
    return performanceTable;
  }

  private AbstractMeasurementEntry createEntry(MeasurementWithCoordinates measurementWithCoordinates) {
    AbstractMeasurementEntry entry;
    Measurement measurement = measurementWithCoordinates.getMeasurement();
    Integer interventionId = measurementWithCoordinates.getInterventionId();
    String dataSource = measurementWithCoordinates.getDataSource();

    if (measurement.getMeasurementTypeURI().equals(ProblemService.DICHOTOMOUS_TYPE_URI)) {
      entry = createBetaDistributionEntry(interventionId, measurement.getVariableConceptUri(), dataSource,
              measurement.getRate(), measurement.getSampleSize());
    } else if (measurement.getMeasurementTypeURI().equals(ProblemService.CONTINUOUS_TYPE_URI)) {
      entry = createNormalDistributionEntry(interventionId, measurement.getVariableConceptUri(), dataSource,
              measurement.getMean(), measurement.getStdDev(), measurement.getSampleSize(), measurement.getStdErr());
    } else {
      throw new IllegalArgumentException("Unknown measurement type: " + measurement.getMeasurementTypeURI());
    }
    return entry;
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
    Outcome measuredOutcome = context.getOutcome();
    String dataSourceUuid = context.getDataSourceUuid();
    return new MeasurementWithCoordinates(measurement, interventionId, dataSourceUuid, measuredOutcome.getSemanticOutcomeUri());
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
}

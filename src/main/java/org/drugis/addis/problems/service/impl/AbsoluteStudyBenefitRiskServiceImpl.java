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
    ArrayList<AbstractMeasurementEntry> performanceTable = new ArrayList<>();
    addAbsolutePerformanceEntries(performanceTable, absoluteMeasurements);
    return performanceTable;
  }

  private Set<MeasurementWithCoordinates> filterAbsoluteMeasurements(Set<MeasurementWithCoordinates> measurementDrugInstancePairs) {
    return measurementDrugInstancePairs.stream().filter(measurement -> measurement.getMeasurement().getReferenceArm() == null).collect(Collectors.toSet());
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
    String dataSourceId = context.getDataSourceId(measuredOutcome.getSemanticOutcomeUri());
    return new MeasurementWithCoordinates(measurement, interventionId, dataSourceId, measuredOutcome.getSemanticOutcomeUri());
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

package org.drugis.addis.problems.service.impl;

import org.drugis.addis.analyses.model.BenefitRiskStudyOutcomeInclusion;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.problems.model.SingleStudyContext;
import org.drugis.addis.problems.service.*;
import org.drugis.addis.problems.service.model.*;
import org.drugis.addis.trialverse.model.trialdata.*;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.*;

import static java.util.stream.Collectors.toList;

@Service
public class ContrastStudyBenefitRiskServiceImpl implements ContrastStudyBenefitRiskService {
  @Override
  public List<AbstractMeasurementEntry> buildContrastPerformanceTable(
          List<BenefitRiskStudyOutcomeInclusion> inclusions,
          URI defaultMoment,
          SingleStudyContext context,
          List<TrialDataArm> matchedArms
  ) {
    List<BenefitRiskStudyOutcomeInclusion> contrastInclusions = getContrastInclusionsWithBaseline(inclusions, context);
    return createContrastEntries(contrastInclusions, matchedArms, defaultMoment, context);

  }

  private List<AbstractMeasurementEntry> createContrastEntries(
          List<BenefitRiskStudyOutcomeInclusion> inclusions,
          List<TrialDataArm> arms,
          URI defaultMoment,
          SingleStudyContext context
  ) {
    return inclusions.stream()
            .map(inclusion -> createContrastEntry(inclusion, arms, defaultMoment, context))
            .collect(toList());
  }

  private AbstractMeasurementEntry createContrastEntry(
          BenefitRiskStudyOutcomeInclusion inclusion,
          List<TrialDataArm> arms,
          URI defaultMoment,
          SingleStudyContext context) {
    Outcome currentOutcome = context.getOutcome(inclusion.getOutcomeId());

    String criterion = currentOutcome.getSemanticOutcomeUri().toString();
    String dataSource = context.getDataSourceId(currentOutcome.getSemanticOutcomeUri());
    RelativePerformance performance = getRelativePerformance(inclusion, arms, defaultMoment, currentOutcome);
    return new RelativePerformanceEntry(criterion, dataSource, performance);
  }

  private boolean hasContrastMeasurementsForOutcome(Set<Measurement> measurements, Outcome outcome) {
    return measurements.stream().anyMatch(measurement -> {
      Boolean isMeasurementForOutcome = measurement.getVariableConceptUri().equals(outcome.getSemanticOutcomeUri());
      Boolean isContrastMeasurement = measurement.getReferenceArm() != null;
      return isContrastMeasurement && isMeasurementForOutcome;
    });
  }

  private RelativePerformance getRelativePerformance(
          BenefitRiskStudyOutcomeInclusion inclusion,
          List<TrialDataArm> arms,
          URI defaultMoment,
          Outcome currentOutcome
  ) {
    RelativePerformanceParameters parameters = getRelativePerformanceParameters(
            inclusion, arms, currentOutcome, defaultMoment);
    Measurement measurement = findNonReferenceArmMeasurement(arms, defaultMoment, currentOutcome);
    String type = getMeasurementType(measurement);
    return new RelativePerformance(type, parameters);
  }

  private String getMeasurementType(Measurement measurement) {
    if (measurement.getOddsRatio() != null) {
      return "relative-logit-normal";
    }
    if (measurement.getHazardRatio() != null) {
      return "relative-survival";
    }
    if (measurement.getMeanDifference() != null) {
      return "relative-normal";
    }
    return "no baseline possible";
  }

  private RelativePerformanceParameters getRelativePerformanceParameters(
          BenefitRiskStudyOutcomeInclusion inclusion,
          List<TrialDataArm> arms, Outcome currentOutcome,
          URI defaultMeasurementMoment
  ) {
    Relative relative = getRelative(arms, currentOutcome, defaultMeasurementMoment);
    String baseline = inclusion.getBaseline();
    return new RelativePerformanceParameters(baseline, relative);
  }

  private Relative getRelative(
          List<TrialDataArm> arms,
          Outcome outcome,
          URI defaultMoment) {
    Measurement nonReferenceArmMeasurement = findNonReferenceArmMeasurement(arms, defaultMoment, outcome);
    List<TrialDataArm> contrastArms = arms
            .stream()
            .filter(arm -> arm.getUri().equals(nonReferenceArmMeasurement.getReferenceArm()) ||
                    hasContrastMeasurementsForOutcome(arm.getMeasurementsForMoment(defaultMoment), outcome))
            .collect(toList());
    String referenceId = getReferenceId(arms, nonReferenceArmMeasurement.getReferenceArm());

    List<String> rowNames = getRowNames(contrastArms, referenceId);
    Map<String, Double> mu = getMu(arms, outcome, defaultMoment, referenceId);
    CovarianceMatrix cov = getCovarianceMatrix(contrastArms, outcome, defaultMoment, rowNames, referenceId);
    return new Relative("dmnorm", mu, cov);
  }

  private CovarianceMatrix getCovarianceMatrix(
          List<TrialDataArm> arms,
          Outcome outcome,
          URI defaultMoment,
          List<String> rowNames,
          String referenceId) {
    List<List<Double>> data = getCovarianceData(arms, defaultMoment, outcome, rowNames, referenceId);
    return new CovarianceMatrix(rowNames, rowNames, data);
  }

  private String getReferenceId(List<TrialDataArm> arms, URI referenceArmUri) {
    TrialDataArm referenceArm = getReferenceArm(arms, referenceArmUri);
    return getArmId(referenceArm);
  }

  private Measurement findNonReferenceArmMeasurement(List<TrialDataArm> arms, URI defaultMoment, Outcome outcome) {
    for (TrialDataArm arm : arms) {
      Set<Measurement> measurements = arm.getMeasurementsForMoment(defaultMoment);
      for (Measurement measurement : measurements) {
        if (isContrastMeasurementForOutcome(measurement, outcome)) {
          return measurement;
        }
      }
    }
    return null;
  }

  private boolean isContrastMeasurementForOutcome(Measurement measurement, Outcome outcome) {
    Boolean isMeasurementForOutcome = measurement.getVariableConceptUri().equals(outcome.getSemanticOutcomeUri());
    Boolean isContrastMeasurement = measurement.getReferenceArm() != null && !measurement.getArmUri().equals(measurement.getReferenceArm());
    return isMeasurementForOutcome && isContrastMeasurement;
  }

  private List<List<Double>> getCovarianceData(
          List<TrialDataArm> arms,
          URI defaultMeasurementMoment,
          Outcome currentOutcome,
          List<String> rowNames,
          String referenceId) {
    List<List<Double>> data = createNewMatrix(rowNames);
    rowNames.forEach(rowId ->
            rowNames
                    .forEach(columnId -> {
                      Double value = getCovarianceValue(arms, defaultMeasurementMoment, currentOutcome, rowId, columnId, referenceId);
                      data.get(rowNames.indexOf(rowId)).set(rowNames.indexOf(columnId), value);
                    })
    );
    return data;
  }

  private Double getCovarianceValue(
          List<TrialDataArm> arms,
          URI defaultMeasurementMoment,
          Outcome currentOutcome,
          String rowId,
          String columnId,
          String referenceId
  ) {
    if (referenceId.equals(rowId) || referenceId.equals(columnId)) {
      return 0.0;
    } else {
      TrialDataArm arm = findArm(arms, rowId);
      Measurement measurement = getMeasurement(defaultMeasurementMoment, arm, currentOutcome);
      return getMatrixValueForCoordinate(measurement, rowId, columnId);
    }
  }

  private Double getMatrixValueForCoordinate(Measurement measurement, String rowId, String columnId) {
    if (rowId.equals(columnId)) {
      return measurement.getStdErr() * measurement.getStdErr();
    } else {
      return measurement.getReferenceStdErr() * measurement.getReferenceStdErr();
    }
  }

  private List<List<Double>> createNewMatrix(List<String> rowNames) {
    List<List<Double>> data = new ArrayList<>();
    for (int i = 0; i < rowNames.size(); ++i) {
      List<Double> row = new ArrayList<>(rowNames.size());
      for (int j = 0; j < rowNames.size(); ++j) {
        row.add(0.0);
      }
      data.add(row);
    }
    return data;
  }

  private Map<String, Double> getMu(
          List<TrialDataArm> arms,
          Outcome outcome,
          URI defaultMoment,
          String referenceId
  ) {
    Map<String, Double> mu = new HashMap<>();
    mu.put(referenceId, 0.0);
    arms.forEach(arm -> {
      String interventionId = getArmId(arm);
      if (!interventionId.equals(referenceId)) {
        Measurement measurement = getMeasurement(defaultMoment, arm, outcome);
        mu.put(interventionId, getValue(measurement));
      }
    });
    return mu;
  }


  private List<String> getRowNames(List<TrialDataArm> arms, String referenceId) {
    List<String> rowNames = new ArrayList<>();
    arms.forEach(arm -> {
      String interventionId = getArmId(arm);
      rowNames.add(interventionId);
    });
    Collections.swap(rowNames, 0, rowNames.indexOf(referenceId));
    return rowNames;
  }

  private TrialDataArm getReferenceArm(List<TrialDataArm> arms, URI referenceArmUri) {
    return arms.stream()
            .filter(arm -> referenceArmUri.equals(arm.getUri()))
            .collect(toList())
            .get(0);
  }

  private TrialDataArm findArm(List<TrialDataArm> arms, String rowId) {
    return arms
            .stream()
            .filter(arm -> getArmId(arm).equals(rowId))
            .iterator()
            .next();
  }

  private String getArmId(TrialDataArm arm) {
    return arm.getMatchedProjectInterventionIds().iterator().next().toString();
  }

  private Measurement getMeasurement(URI defaultMeasurementMoment, TrialDataArm arm, Outcome outcome) {
    Set<Measurement> measurements = arm.getMeasurementsForMoment(defaultMeasurementMoment);
    List<Measurement> measurementsForOutcome = measurements
            .stream()
            .filter(measurement ->
                    measurement.getVariableConceptUri().equals(outcome.getSemanticOutcomeUri()))
            .collect(toList());
    return measurementsForOutcome.iterator().next();
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


  @Override
  public List<BenefitRiskStudyOutcomeInclusion> getContrastInclusionsWithBaseline(
          List<BenefitRiskStudyOutcomeInclusion> inclusions, SingleStudyContext context) {
    return inclusions
            .stream()
            .filter(inclusion -> context.getOutcome(inclusion.getOutcomeId()) != null)
            .filter(inclusion -> inclusion.getBaseline() != null)
            .collect(toList());
  }
}

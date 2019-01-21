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
    List<TrialDataArm> contrastArms = getContrastArms(matchedArms);
    if (contrastArms.size() == 0) {
      return Collections.emptyList();
    } else {
      return createContrastEntries(inclusions, contrastArms, defaultMoment, context);
    }
  }

  private List<TrialDataArm> getContrastArms(List<TrialDataArm> arms) {
    return arms.stream().filter(arm -> arm.getReferenceArm() != null).collect(toList());
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

  private RelativePerformance getRelativePerformance(
          BenefitRiskStudyOutcomeInclusion inclusion,
          List<TrialDataArm> arms,
          URI defaultMoment,
          Outcome currentOutcome
  ) {
    TrialDataArm nonReferenceArm = getNonReferenceArm(arms);
    Measurement randomMeasurement = getRandomMeasurement(defaultMoment, nonReferenceArm);

    RelativePerformanceParameters parameters =
            getRelativePerformanceParameters(inclusion, arms, currentOutcome, defaultMoment, nonReferenceArm);

    String type = getMeasurementType(randomMeasurement);
    return new RelativePerformance(type, parameters);
  }

  private RelativePerformanceParameters getRelativePerformanceParameters(BenefitRiskStudyOutcomeInclusion inclusion, List<TrialDataArm> arms, Outcome currentOutcome, URI defaultMeasurementMoment, TrialDataArm nonReferenceArm) {
    Relative relative = getRelative(arms, currentOutcome, defaultMeasurementMoment, nonReferenceArm);
    String baseline = inclusion.getBaseline();
    return new RelativePerformanceParameters(baseline, relative);
  }

  private Relative getRelative(List<TrialDataArm> arms, Outcome currentOutcome, URI defaultMeasurementMoment, TrialDataArm nonReferenceArm) {
    List<String> rowNames = getRowNames(arms);
    Map<String, Double> mu = getMu(arms, currentOutcome, defaultMeasurementMoment);
    CovarianceMatrix cov = getCovarianceMatrix(arms, currentOutcome, defaultMeasurementMoment, nonReferenceArm, rowNames);
    return new Relative("dmnorm", mu, cov);
  }

  private CovarianceMatrix getCovarianceMatrix(List<TrialDataArm> arms, Outcome currentOutcome, URI defaultMeasurementMoment, TrialDataArm nonReferenceArm, List<String> rowNames) {
    Double referenceStdErr = nonReferenceArm.getReferenceStdErr();
    String referenceId = getReferenceId(arms);
    List<List<Double>> data = getCovarianceData(arms, defaultMeasurementMoment, referenceId, referenceStdErr, currentOutcome, rowNames);
    return new CovarianceMatrix(rowNames, rowNames, data);
  }

  private List<List<Double>> getCovarianceData(List<TrialDataArm> arms, URI defaultMeasurementMoment, String referenceId, Double referenceStdErr, Outcome currentOutcome, List<String> rowNames) {
    List<List<Double>> data = createNewMatrix(rowNames);
    rowNames.forEach(rowId ->
            rowNames.stream()
                    .filter(columnId -> isNotFirstRow(referenceId, rowId) && isNotFirstColumn(referenceId, columnId))
                    .forEach(columnId -> {
                      Double value = getMatrixValueForCoordinate(arms, defaultMeasurementMoment, referenceStdErr, currentOutcome, rowNames, data, rowId, columnId);
                      data.get(rowNames.indexOf(rowId)).set(rowNames.indexOf(columnId), value);
                    })
    );
    return data;
  }

  private boolean isNotFirstColumn(String referenceId, String columnName) {
    return !referenceId.equals(columnName);
  }

  private boolean isNotFirstRow(String referenceId, String rowId) {
    return !referenceId.equals(rowId);
  }

  private Double getMatrixValueForCoordinate(List<TrialDataArm> arms, URI defaultMeasurementMoment, Double referenceStdErr, Outcome currentOutcome, List<String> rowNames, List<List<Double>> data, String rowId, String columnName) {
    Double value;
    if (rowId.equals(columnName)) {
      value = getDiagonalValue(arms, defaultMeasurementMoment, currentOutcome, rowId);
    } else {
      value = referenceStdErr * referenceStdErr;
    }
    return value;

  }

  private Double getDiagonalValue(List<TrialDataArm> arms, URI defaultMeasurementMoment, Outcome currentOutcome, String rowId) {
    Double value;
    TrialDataArm arm = findArm(arms, rowId);
    Measurement measurement = getMeasurement(defaultMeasurementMoment, arm, currentOutcome);
    value = measurement.getStdErr() * measurement.getStdErr();
    return value;
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

  private Measurement getRandomMeasurement(URI defaultMoment, TrialDataArm nonReferenceArm) {
    return nonReferenceArm.getMeasurementsForMoment(defaultMoment).iterator().next();
  }

  private TrialDataArm getNonReferenceArm(List<TrialDataArm> arms) {
    return arms.stream()
            .filter(arm -> !arm.getReferenceArm().equals(arm.getUri()))
            .collect(toList()).get(0);
  }

  private Map<String, Double> getMu(List<TrialDataArm> arms, Outcome currentOutcome, URI defaultMeasurementMoment) {
    String referenceId = getReferenceId(arms);
    Map<String, Double> mu = new HashMap<>();
    mu.put(referenceId, 0.0);
    arms.forEach(arm -> {
      String interventionId = getArmId(arm);
      if (!interventionId.equals(referenceId)) {
        Measurement measurement = getMeasurement(defaultMeasurementMoment, arm, currentOutcome);
        mu.put(interventionId, getValue(measurement));
      }
    });
    return mu;
  }

  private String getReferenceId(List<TrialDataArm> arms) {
    TrialDataArm referenceArm = getReferenceArm(arms);
    return getArmId(referenceArm);
  }

  private List<String> getRowNames(List<TrialDataArm> arms) {
    String referenceId = getReferenceId(arms);
    List<String> rowNames = new ArrayList<>();
    arms.forEach(arm -> {
      String interventionId = getArmId(arm);
      rowNames.add(interventionId);
    });
    Collections.swap(rowNames, 0, rowNames.indexOf(referenceId));
    return rowNames;
  }

  private TrialDataArm getReferenceArm(List<TrialDataArm> arms) {
    return arms.stream()
            .filter(arm -> arm.getReferenceArm().equals(arm.getUri()))
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
            .filter(meas ->
                    meas.getVariableConceptUri().equals(outcome.getSemanticOutcomeUri()))
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

  @Override
  public List<BenefitRiskStudyOutcomeInclusion> getContrastInclusionsWithBaseline(List<BenefitRiskStudyOutcomeInclusion> inclusions, SingleStudyContext context) {
    return inclusions
            .stream()
            .filter(inclusion -> isContrastInclusion(inclusion, context.getOutcomesByUri()))
            .filter(inclusion -> inclusion.getBaseline() != null)
            .collect(toList());
  }

  private boolean isContrastInclusion(BenefitRiskStudyOutcomeInclusion inclusion, Map<URI, Outcome> outcomesByUri) {
    return outcomesByUri.values().stream().anyMatch(outcome -> outcome.getId().equals(inclusion.getOutcomeId()));
  }


}

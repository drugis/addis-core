package org.drugis.addis.problems.service.impl;

import org.drugis.addis.problems.model.problemEntry.*;
import org.drugis.addis.trialverse.model.trialdata.Measurement;
import org.springframework.stereotype.Service;

import static org.drugis.addis.problems.service.ProblemService.CONTINUOUS_TYPE_URI;
import static org.drugis.addis.problems.service.ProblemService.DICHOTOMOUS_TYPE_URI;
import static org.drugis.addis.problems.service.ProblemService.SURVIVAL_TYPE_URI;

@Service
public class NetworkMetaAnalysisEntryBuilder {

  public AbstractProblemEntry buildAbsoluteEntry(String studyName, Integer treatmentId, Measurement measurement) {
    return getAbsoluteProblemEntry(studyName, treatmentId, measurement);
  }

  public AbstractProblemEntry buildContrastEntry(String studyName, Integer treatmentId, Measurement measurement) {
    return getContrastProblemEntry(studyName, treatmentId, measurement);
  }

  private AbstractProblemEntry getContrastProblemEntry(
          String studyName, Integer treatmentId,
          Measurement measurement) {
    if (measurement.getMeasurementTypeURI().equals(CONTINUOUS_TYPE_URI)) {
      return getContrastContinuousProblemEntry(studyName, treatmentId, measurement);
    } else if (measurement.getMeasurementTypeURI().equals(DICHOTOMOUS_TYPE_URI)) {
      return getContrastDichotomousProblemEntry(studyName, treatmentId, measurement);
    } else if (measurement.getMeasurementTypeURI().equals(SURVIVAL_TYPE_URI)) {
      return getContrastSurvivalProblemEntry(studyName, treatmentId, measurement);
    }
    throw new RuntimeException("unknown measurement type");
  }

  private AbstractProblemEntry getContrastContinuousProblemEntry(
          String studyName, Integer treatmentId,
          Measurement measurement) {
    if (measurement.getStandardizedMeanDifference() != null) {
      Double standardizedMeanDifference = measurement.getStandardizedMeanDifference();
      return new ContrastSMDProblemEntry(studyName, treatmentId,
              standardizedMeanDifference,
              measurement.getStdErr());
    } else if (measurement.getMeanDifference() != null) {
      return new ContrastMDProblemEntry(studyName, treatmentId,
              measurement.getMeanDifference(),
              measurement.getStdErr());
    }
    throw new RuntimeException("unknown continuous contrast result property");
  }


  private AbstractProblemEntry getContrastDichotomousProblemEntry(
          String studyName, Integer treatmentId,
          Measurement measurement) {
    if (measurement.getOddsRatio() != null) {
      return new ContrastDichotomousOddsProblemEntry(studyName, treatmentId,
              measurement.getOddsRatio(),
              measurement.getStdErr());
    } else if (measurement.getRiskRatio() != null) {
      return new ContrastDichotomousRiskProblemEntry(studyName, treatmentId,
              measurement.getRiskRatio(),
              measurement.getStdErr());
    }
    throw new RuntimeException("unknown dichotomous contrast result property");
  }

  private AbstractProblemEntry getContrastSurvivalProblemEntry(
          String studyName, Integer treatmentId,
          Measurement measurement) {
    if (measurement.getHazardRatio() != null) {
      return new ContrastSurvivalHazardProblemEntry(studyName, treatmentId,
              measurement.getHazardRatio(),
              measurement.getStdErr());
    }
    throw new RuntimeException("unknown survival contrast result property");
  }

  private AbstractProblemEntry getAbsoluteProblemEntry(String studyName, Integer treatmentId, Measurement measurement) {
    if (measurement.getMeasurementTypeURI().equals(CONTINUOUS_TYPE_URI)) {
      return getAbsoluteContinuousProblemEntry(studyName, treatmentId, measurement);
    } else if (measurement.getMeasurementTypeURI().equals(DICHOTOMOUS_TYPE_URI)) {
      return getAbsoluteDichotomousProblemEntry(studyName, treatmentId, measurement);
    } else if (measurement.getMeasurementTypeURI().equals(SURVIVAL_TYPE_URI)) {
      return getAbsoluteSurvivalProblemEntry(studyName, treatmentId, measurement);
    }
    throw new RuntimeException("unknown measurement type");
  }

  private AbstractProblemEntry getAbsoluteSurvivalProblemEntry(String studyName, Integer treatmentId, Measurement measurement) {
    Integer rate = measurement.getRate();
    Double exposure = measurement.getExposure();
    String timeScale = measurement.getSurvivalTimeScale();
    return new AbsoluteSurvivalProblemEntry(studyName, treatmentId, timeScale, rate, exposure);
  }

  private AbstractProblemEntry getAbsoluteDichotomousProblemEntry(String studyName, Integer treatmentId, Measurement measurement) {
    Integer sampleSize = measurement.getSampleSize();
    Integer rate = measurement.getRate();
    return new AbsoluteDichotomousProblemEntry(studyName, treatmentId, sampleSize, rate);
  }

  private AbstractProblemEntry getAbsoluteContinuousProblemEntry(String studyName, Integer treatmentId, Measurement measurement) {
    Integer sampleSize = measurement.getSampleSize();
    Double mu = measurement.getMean();
    Double sigma = measurement.getStdDev();
    Double stdErr = measurement.getStdErr();
    if (sigma != null && sampleSize != null) {
      return new AbsoluteContinuousProblemEntry(studyName, treatmentId, sampleSize, mu, sigma);
    } else {
      return new AbsoluteContinuousStdErrProblemEntry(studyName, treatmentId, mu, stdErr);
    }
  }

}

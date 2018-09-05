package org.drugis.addis.problems.service.impl;

import org.drugis.addis.problems.model.*;
import org.drugis.addis.trialverse.model.trialdata.Measurement;
import org.springframework.stereotype.Service;

import static org.drugis.addis.problems.service.ProblemService.CONTINUOUS_TYPE_URI;
import static org.drugis.addis.problems.service.ProblemService.DICHOTOMOUS_TYPE_URI;
import static org.drugis.addis.problems.service.ProblemService.SURVIVAL_TYPE_URI;

@Service
public class NetworkMetaAnalysisEntryBuilder {

  public AbstractNetworkMetaAnalysisProblemEntry build(String studyName, Integer treatmentId, Measurement measurement) {
    Integer sampleSize = measurement.getSampleSize();
    if (measurement.getMeasurementTypeURI().equals(CONTINUOUS_TYPE_URI)) {
      Double mu = measurement.getMean();
      Double sigma = measurement.getStdDev();
      Double stdErr = measurement.getStdErr();
      if (sigma != null && sampleSize != null) {
        return new ContinuousNetworkMetaAnalysisProblemEntry(studyName, treatmentId, sampleSize, mu, sigma);
      } else {
        return new ContinuousStdErrEntry(studyName, treatmentId, mu, stdErr);
      }
    } else if (measurement.getMeasurementTypeURI().equals(DICHOTOMOUS_TYPE_URI)) {
      Integer rate = measurement.getRate();
      return new RateNetworkMetaAnalysisProblemEntry(studyName, treatmentId, sampleSize, rate);
    } else if (measurement.getMeasurementTypeURI().equals(SURVIVAL_TYPE_URI)) {
      Integer rate = measurement.getRate();
      Double exposure = measurement.getExposure();
      String timeScale = measurement.getSurvivalTimeScale();
      return new SurvivalEntry(studyName, treatmentId, timeScale, rate, exposure);
    }
    throw new RuntimeException("unknown measurement type");
  }

}

package org.drugis.addis.trialverse.model.trialdata;

import com.google.common.collect.ImmutableSet;
import org.drugis.addis.trialverse.model.MeasurementMoment;

import java.net.URI;
import java.util.*;

/**
 * Created by connor on 15-5-14.
 */
public class TrialDataStudy {
  private URI studyUri;
  private String name;
  private List<TrialDataArm> trialDataArms = new ArrayList<>();
  private List<CovariateStudyValue> covariateValues = new ArrayList<>();
  private URI defaultMeasurementMoment;
  private Set<MeasurementMoment> measurementMoments = new HashSet<>();

  public TrialDataStudy() {
  }

  public TrialDataStudy(URI studyUri, String name, List<TrialDataArm> arms) {
    this.studyUri = studyUri;
    this.name = name;

    if (arms != null) {
      this.trialDataArms = arms;
    }
  }

  public URI getStudyUri() {
    return studyUri;
  }

  public String getName() {
    return name;
  }

  public List<TrialDataArm> getArms() {
    return trialDataArms;
  }

  public void addCovariateValue(CovariateStudyValue covariateStudyValue) {
    covariateValues.add(covariateStudyValue);
  }

  public List<CovariateStudyValue> getCovariateValues() {
    return covariateValues;
  }

  public URI getDefaultMeasurementMoment() {
    return defaultMeasurementMoment;
  }

  public Set<MeasurementMoment> getMeasurementMoments() {
    return ImmutableSet.copyOf(this.measurementMoments);
  }

  public void addMeasurementMoment(MeasurementMoment measurementMoment) {
    this.measurementMoments.add(measurementMoment);
  }

  public void setDefaultMeasurementMoment(URI defaultMeasurementMoment) {
    this.defaultMeasurementMoment = defaultMeasurementMoment;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TrialDataStudy that = (TrialDataStudy) o;
    return Objects.equals(studyUri, that.studyUri) &&
            Objects.equals(name, that.name) &&
            Objects.equals(trialDataArms, that.trialDataArms) &&
            Objects.equals(covariateValues, that.covariateValues) &&
            Objects.equals(defaultMeasurementMoment, that.defaultMeasurementMoment) &&
            Objects.equals(measurementMoments, that.measurementMoments);
  }

  @Override
  public int hashCode() {

    return Objects.hash(studyUri, name, trialDataArms, covariateValues, defaultMeasurementMoment, measurementMoments);
  }
}
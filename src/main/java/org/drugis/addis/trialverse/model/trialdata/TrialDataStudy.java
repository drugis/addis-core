package org.drugis.addis.trialverse.model.trialdata;

import com.google.common.collect.ImmutableSet;
import org.drugis.addis.trialverse.model.MeasurementMoment;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

  public TrialDataStudy(URI studyUri, String name, List<TrialDataArm> trialDataArms) {
    this.studyUri = studyUri;
    this.name = name;

    if (trialDataArms != null) {
      this.trialDataArms = trialDataArms;
    }
  }

  public URI getStudyUri() {
    return studyUri;
  }

  public String getName() {
    return name;
  }

  public List<TrialDataArm> getTrialDataArms() {
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

    if (!studyUri.equals(that.studyUri)) return false;
    if (!name.equals(that.name)) return false;
    if (!trialDataArms.equals(that.trialDataArms)) return false;
    if (!covariateValues.equals(that.covariateValues)) return false;
    return defaultMeasurementMoment != null ? defaultMeasurementMoment.equals(that.defaultMeasurementMoment) : that.defaultMeasurementMoment == null;

  }

  @Override
  public int hashCode() {
    int result = studyUri.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + trialDataArms.hashCode();
    result = 31 * result + covariateValues.hashCode();
    result = 31 * result + (defaultMeasurementMoment != null ? defaultMeasurementMoment.hashCode() : 0);
    return result;
  }
}
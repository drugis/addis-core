package org.drugis.addis.trialverse.model.trialdata;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by connor on 15-5-14.
 */
public class TrialDataStudy {
  private URI studyUri;
  private String name;

  private List<TrialDataArm> trialDataArms = new ArrayList<>();
  private List<CovariateStudyValue> covariateValues = new ArrayList<>();

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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    TrialDataStudy that = (TrialDataStudy) o;

    if (!studyUri.equals(that.studyUri)) return false;
    if (!name.equals(that.name)) return false;
    if (!trialDataArms.equals(that.trialDataArms)) return false;
    return covariateValues.equals(that.covariateValues);

  }

  @Override
  public int hashCode() {
    int result = studyUri.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + trialDataArms.hashCode();
    result = 31 * result + covariateValues.hashCode();
    return result;
  }
}
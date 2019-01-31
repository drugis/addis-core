package org.drugis.addis.trialverse.model;

import java.util.*;

/**
 * Created by connor on 14-8-14.
 */
public class TreatmentActivity {
  private String treatmentActivityUri;
  private String treatmentActivityType;
  private Set<ActivityApplication> activityApplications = new HashSet<>();
  private Set<AdministeredDrug> administeredDrugs = new HashSet<>();

  public TreatmentActivity(String treatmentActivityUri, String treatmentActivityType) {
    this.treatmentActivityUri = treatmentActivityUri;
    this.treatmentActivityType = treatmentActivityType;
  }

  public String getTreatmentActivityUri() {
    return treatmentActivityUri;
  }

  public String getTreatmentActivityType() {
    return treatmentActivityType;
  }

  public Set<ActivityApplication> getActivityApplications() {
    return activityApplications;
  }

  public Set<AdministeredDrug> getAdministeredDrugs() {
    return administeredDrugs;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TreatmentActivity that = (TreatmentActivity) o;
    return Objects.equals(treatmentActivityUri, that.treatmentActivityUri) &&
            Objects.equals(treatmentActivityType, that.treatmentActivityType) &&
            Objects.equals(activityApplications, that.activityApplications) &&
            Objects.equals(administeredDrugs, that.administeredDrugs);
  }

  @Override
  public int hashCode() {

    return Objects.hash(treatmentActivityUri, treatmentActivityType, activityApplications, administeredDrugs);
  }
}

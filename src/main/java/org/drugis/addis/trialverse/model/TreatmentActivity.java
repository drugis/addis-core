package org.drugis.addis.trialverse.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    if (!(o instanceof TreatmentActivity)) return false;

    TreatmentActivity that = (TreatmentActivity) o;

    if (!activityApplications.equals(that.activityApplications)) return false;
    if (!administeredDrugs.equals(that.administeredDrugs)) return false;
    if (!treatmentActivityType.equals(that.treatmentActivityType)) return false;
    if (!treatmentActivityUri.equals(that.treatmentActivityUri)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = treatmentActivityUri.hashCode();
    result = 31 * result + treatmentActivityType.hashCode();
    result = 31 * result + activityApplications.hashCode();
    result = 31 * result + administeredDrugs.hashCode();
    return result;
  }
}

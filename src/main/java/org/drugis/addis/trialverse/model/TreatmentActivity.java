package org.drugis.addis.trialverse.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by connor on 14-8-14.
 */
public class TreatmentActivity {
  private String treatmentActivityUri;
  private String treatmentActivityType;
  private String epochUid;
  private String armUid;

  private List<AdministeredDrug> administeredDrugs = new ArrayList<>();

  public TreatmentActivity(String treatmentActivityUri, String treatmentActivityType, String epochUid, String armUid, List<AdministeredDrug> administeredDrugs) {
    this.treatmentActivityUri = treatmentActivityUri;
    this.treatmentActivityType = treatmentActivityType;
    this.epochUid = epochUid;
    this.armUid = armUid;
    if (administeredDrugs != null) {
      this.administeredDrugs = administeredDrugs;
    }
  }

  public String getTreatmentActivityUri() {
    return treatmentActivityUri;
  }

  public String getTreatmentActivityType() {
    return treatmentActivityType;
  }

  public String getEpochUid() {
    return epochUid;
  }

  public String getArmUid() {
    return armUid;
  }

  public List<AdministeredDrug> getAdministeredDrugs() {
    return administeredDrugs;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TreatmentActivity)) return false;

    TreatmentActivity that = (TreatmentActivity) o;

    if (!administeredDrugs.equals(that.administeredDrugs)) return false;
    if (!armUid.equals(that.armUid)) return false;
    if (!epochUid.equals(that.epochUid)) return false;
    if (!treatmentActivityType.equals(that.treatmentActivityType)) return false;
    if (!treatmentActivityUri.equals(that.treatmentActivityUri)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = treatmentActivityUri.hashCode();
    result = 31 * result + treatmentActivityType.hashCode();
    result = 31 * result + epochUid.hashCode();
    result = 31 * result + armUid.hashCode();
    result = 31 * result + administeredDrugs.hashCode();
    return result;
  }
}

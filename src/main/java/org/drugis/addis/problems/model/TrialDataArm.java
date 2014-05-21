package org.drugis.addis.problems.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by connor on 9-5-14.
 */
public class TrialDataArm {
  private Long id;
  private String name;
  private Long study;
  private Long drugId;

  private List<Measurement> measurements = new ArrayList<>();

  public TrialDataArm() {
  }

  public TrialDataArm(Long id, Long study, String name, Long drugId, List<Measurement> measurements) {
    this.id = id;
    this.study = study;
    this.name = name;
    this.drugId = drugId;
    this.measurements = measurements;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Long getStudy() {
    return study;
  }

  public Long getDrugId() {
    return drugId;
  }

  public List<Measurement> getMeasurements() {
    return measurements;
  }

  public void setMeasurements(List<Measurement> measurements) {
    this.measurements = measurements != null ? this.measurements = measurements : new ArrayList<Measurement>();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TrialDataArm)) return false;

    TrialDataArm that = (TrialDataArm) o;

    if (!id.equals(that.id)) return false;
    if (!measurements.equals(that.measurements)) return false;
    if (!name.equals(that.name)) return false;
    if (!study.equals(that.study)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + study.hashCode();
    result = 31 * result + measurements.hashCode();
    return result;
  }
}

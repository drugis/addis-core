package org.drugis.addis.trialverse.model;

public class Measurement {

  private String studyUid;
  private String variableUid;
  private String armUid;
  private Integer sampleSize;
  private Long rate;
  private Double stdDev;
  private Double mean;

  public Measurement() {
  }

  public Measurement(String studyUid, String variableUid, String armUid, Integer sampleSize, Long rate, Double stdDev, Double mean) {
    this.studyUid = studyUid;
    this.variableUid = variableUid;
    this.armUid = armUid;
    this.sampleSize = sampleSize;
    this.rate = rate;
    this.stdDev = stdDev;
    this.mean = mean;
  }

  public String getStudyUid() {
    return studyUid;
  }

  public String getVariableUid() {
    return variableUid;
  }

  public String getArmUid() {
    return armUid;
  }

  public Integer getSampleSize() {
    return sampleSize;
  }

  public Long getRate() {
    return rate;
  }

  public Double getStdDev() {
    return stdDev;
  }

  public Double getMean() {
    return mean;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Measurement)) return false;

    Measurement that = (Measurement) o;

    if (!armUid.equals(that.armUid)) return false;
    if (mean != null ? !mean.equals(that.mean) : that.mean != null) return false;
    if (rate != null ? !rate.equals(that.rate) : that.rate != null) return false;
    if (!sampleSize.equals(that.sampleSize)) return false;
    if (stdDev != null ? !stdDev.equals(that.stdDev) : that.stdDev != null) return false;
    if (!studyUid.equals(that.studyUid)) return false;
    if (!variableUid.equals(that.variableUid)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = studyUid.hashCode();
    result = 31 * result + variableUid.hashCode();
    result = 31 * result + armUid.hashCode();
    result = 31 * result + sampleSize.hashCode();
    result = 31 * result + (rate != null ? rate.hashCode() : 0);
    result = 31 * result + (stdDev != null ? stdDev.hashCode() : 0);
    result = 31 * result + (mean != null ? mean.hashCode() : 0);
    return result;
  }
}

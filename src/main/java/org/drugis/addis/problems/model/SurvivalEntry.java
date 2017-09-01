package org.drugis.addis.problems.model;

public class SurvivalEntry extends AbstractNetworkMetaAnalysisProblemEntry {
  private final String timeScale;
  private final Integer responders;
  private final Double exposure;

  public SurvivalEntry(String studyName, Integer treatmentId, String timeScale, Integer responders, Double exposure) {
    super(studyName, treatmentId);
    this.timeScale = timeScale;
    this.responders = responders;
    this.exposure = exposure;
  }

  public Integer getResponders() {
    return responders;
  }

  public Double getExposure() {
    return exposure;
  }

  public String getTimeScale() {
    return timeScale;
  }

  @Override
  public boolean hasMissingValues() {
    return responders == null || exposure == null || timeScale == null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    SurvivalEntry that = (SurvivalEntry) o;

    if (timeScale != null ? !timeScale.equals(that.timeScale) : that.timeScale != null) return false;
    if (responders != null ? !responders.equals(that.responders) : that.responders != null) return false;
    return exposure != null ? exposure.equals(that.exposure) : that.exposure == null;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (timeScale != null ? timeScale.hashCode() : 0);
    result = 31 * result + (responders != null ? responders.hashCode() : 0);
    result = 31 * result + (exposure != null ? exposure.hashCode() : 0);
    return result;
  }
}

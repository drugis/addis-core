package org.drugis.addis.problems.model.problemEntry;

import java.util.Objects;

public class AbsoluteSurvivalProblemEntry extends AbstractProblemEntry {
  private final String timeScale;
  private final Integer responders;
  private final Double exposure;

  public AbsoluteSurvivalProblemEntry(String studyName, Integer treatmentId, String timeScale, Integer responders, Double exposure) {
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
    AbsoluteSurvivalProblemEntry that = (AbsoluteSurvivalProblemEntry) o;
    return Objects.equals(timeScale, that.timeScale) &&
            Objects.equals(responders, that.responders) &&
            Objects.equals(exposure, that.exposure);
  }

  @Override
  public int hashCode() {

    return Objects.hash(super.hashCode(), timeScale, responders, exposure);
  }
}

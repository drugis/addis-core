package org.drugis.addis.problems.model.problemEntry;

import java.util.Objects;

/**
 * Created by daan on 21-5-14.
 */
public class AbsoluteDichotomousProblemEntry extends AbstractProblemEntry {
  private Integer responders;
  private Integer sampleSize;

  public AbsoluteDichotomousProblemEntry(String study, Integer treatment, Integer samplesize, Integer responders) {
    super(study, treatment);
    this.sampleSize = samplesize;
    this.responders = responders;
  }

  public Integer getSampleSize() {
    return sampleSize;
  }

  public Integer getResponders() {
    return responders;
  }

  @Override
  public boolean hasMissingValues() {
    return responders == null || sampleSize == null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    AbsoluteDichotomousProblemEntry that = (AbsoluteDichotomousProblemEntry) o;
    return Objects.equals(responders, that.responders) &&
            Objects.equals(sampleSize, that.sampleSize);
  }

  @Override
  public int hashCode() {

    return Objects.hash(super.hashCode(), responders, sampleSize);
  }
}

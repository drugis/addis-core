package org.drugis.addis.ordering;

import com.fasterxml.jackson.annotation.JsonRawValue;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Objects;

@Entity
public class Ordering {
  @Id
  private Integer analysisId;

  @JsonRawValue
  private String ordering;

  public Ordering() {
  }

  public Ordering(Integer analysisId, String ordering) {
    this.analysisId = analysisId;
    this.ordering = ordering;
  }

  public Integer getAnalysisId() {
    return analysisId;
  }

  public String getOrdering() {
    return ordering;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Ordering ordering1 = (Ordering) o;
    return Objects.equals(analysisId, ordering1.analysisId) &&
            Objects.equals(ordering, ordering1.ordering);
  }

  @Override
  public int hashCode() {

    return Objects.hash(analysisId, ordering);
  }
}

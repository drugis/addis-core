package org.drugis.addis.ordering;

import com.fasterxml.jackson.annotation.JsonRawValue;

import javax.persistence.Entity;
import javax.persistence.Id;

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

  public void setAnalysisId(Integer analysisId) {
    this.analysisId = analysisId;
  }

  public void setOrdering(String ordering) {
    this.ordering = ordering;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Ordering ordering1 = (Ordering) o;

    if (!analysisId.equals(ordering1.analysisId)) return false;
    return ordering.equals(ordering1.ordering);
  }

  public Integer getAnalysisId() {
    return analysisId;
  }

  public String getOrdering() {
    return ordering;
  }

  @Override
  public int hashCode() {

    int result = analysisId.hashCode();
    result = 31 * result + ordering.hashCode();
    return result;
  }
}

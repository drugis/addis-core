package org.drugis.addis.ordering;

import com.fasterxml.jackson.annotation.JsonRawValue;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Ordering {
  @Id
  private Integer analysisId;

  String[] alternatives;

  String[] criteria;

  public Ordering() {
  }

  public Ordering(Integer analysisId, String[] alternatives, String[] criteria) {
    this.analysisId = analysisId;
    this.alternatives = alternatives;
    this.criteria = criteria;
  }

  public void setAnalysisId(Integer analysisId) {
    this.analysisId = analysisId;
  }
}

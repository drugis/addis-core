package org.drugis.addis.toggledColumns;

import com.fasterxml.jackson.annotation.JsonRawValue;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class ToggledColumns {
  @Id
  private Integer analysisId;

  @JsonRawValue
  private String toggledColumns;


  public ToggledColumns() {
  }

  public ToggledColumns(Integer analysisId, String toggledColumns) {
    this.analysisId = analysisId;
    this.toggledColumns = toggledColumns;
  }

  public void setAnalysisId(Integer analysisId) {
    this.analysisId = analysisId;
  }

  public void setToggledColumns(String toggledColumns) {
    this.toggledColumns = toggledColumns;
  }

}

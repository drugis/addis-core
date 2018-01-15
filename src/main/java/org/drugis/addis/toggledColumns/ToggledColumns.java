package org.drugis.addis.toggledColumns;

import com.fasterxml.jackson.annotation.JsonRawValue;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Objects;

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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ToggledColumns that = (ToggledColumns) o;
    return Objects.equals(analysisId, that.analysisId) &&
            Objects.equals(toggledColumns, that.toggledColumns);
  }

  @Override
  public int hashCode() {
    return Objects.hash(analysisId, toggledColumns);
  }

  public String getToggledColumns() {
    return toggledColumns;
  }

  public Integer getAnalysisId() {
    return analysisId;
  }
}

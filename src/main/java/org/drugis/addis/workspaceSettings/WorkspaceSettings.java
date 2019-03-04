package org.drugis.addis.workspaceSettings;

import com.fasterxml.jackson.annotation.JsonRawValue;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Objects;

@Entity
public class WorkspaceSettings {
  @Id
  private Integer analysisId;

  @JsonRawValue
  private String settings;

  public WorkspaceSettings() {
  }

  public WorkspaceSettings(Integer analysisId, String settings) {
    this.analysisId = analysisId;
    this.settings = settings;
  }

  public Integer getAnalysisId() {
    return analysisId;
  }

  public String getSettings() {
    return settings;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    WorkspaceSettings that = (WorkspaceSettings) o;
    return Objects.equals(analysisId, that.analysisId) &&
            Objects.equals(settings, that.settings);
  }

  @Override
  public int hashCode() {

    return Objects.hash(analysisId, settings);
  }
}

package org.drugis.addis.analyses.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Created by connor on 3/11/14.
 */
public class AnalysisCommand {
  private Integer projectId;
  private String title;
  private String type;

  private List<Integer> selectedOutcomeIds = new ArrayList<>();

  public AnalysisCommand() {
  }

  public AnalysisCommand(Integer projectId, String title, String type) {
    this.projectId = projectId;
    this.title = title;
    this.type = type;
  }

  public AnalysisCommand(Integer projectId, String title, String type, List<Integer> selectedOutcomeIds) {
    this.projectId = projectId;
    this.title = title;
    this.type = type;
    if (selectedOutcomeIds != null) {
      this.selectedOutcomeIds = selectedOutcomeIds;
    }
  }

  public Integer getProjectId() {
    return projectId;
  }

  public String getTitle() {
    return title;
  }

  public String getType() {
    return type;
  }

  public List<Integer> getSelectedOutcomeIds() {
    return Collections.unmodifiableList(selectedOutcomeIds);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AnalysisCommand that = (AnalysisCommand) o;
    return Objects.equals(projectId, that.projectId) &&
            Objects.equals(title, that.title) &&
            Objects.equals(type, that.type) &&
            Objects.equals(selectedOutcomeIds, that.selectedOutcomeIds);
  }

  @Override
  public int hashCode() {

    return Objects.hash(projectId, title, type, selectedOutcomeIds);
  }
}

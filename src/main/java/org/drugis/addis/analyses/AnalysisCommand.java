package org.drugis.addis.analyses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by connor on 3/11/14.
 */
public class AnalysisCommand {
  private Integer projectId;
  private String name;
  private String type;

  private List<Integer> selectedOutcomeIds = new ArrayList<>();

  public AnalysisCommand() {
  }

  public AnalysisCommand(Integer projectId, String name, String type) {
    this.projectId = projectId;
    this.name = name;
    this.type = type;
  }

  public AnalysisCommand(Integer projectId, String name, String type, List<Integer> selectedOutcomeIds) {
    this.projectId = projectId;
    this.name = name;
    this.type = type;
    if(selectedOutcomeIds != null) {
      this.selectedOutcomeIds = selectedOutcomeIds;
    }
  }

  public Integer getProjectId() {
    return projectId;
  }

  public String getName() {
    return name;
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

    if (!name.equals(that.name)) return false;
    if (!projectId.equals(that.projectId)) return false;
    if (!selectedOutcomeIds.equals(that.selectedOutcomeIds)) return false;
    if (!type.equals(that.type)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = projectId.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + type.hashCode();
    result = 31 * result + selectedOutcomeIds.hashCode();
    return result;
  }
}

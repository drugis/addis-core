package org.drugis.addis.projects;

import org.apache.commons.lang.StringUtils;

/**
 * Created by daan on 3/5/14.
 */
public class ProjectCommand {
  private String name;
  private String description;
  private Integer trialverseId;

  public ProjectCommand() {
  }

  public ProjectCommand(String name, String description, Integer trialverseId) {
    this.name = name;
    this.description = description;
    this.trialverseId = trialverseId;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description == null ? StringUtils.EMPTY : description;
  }

  public Integer getTrialverseId() {
    return trialverseId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ProjectCommand that = (ProjectCommand) o;

    if (description != null ? !description.equals(that.description) : that.description != null) return false;
    if (!name.equals(that.name)) return false;
    if (!trialverseId.equals(that.trialverseId)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + (description != null ? description.hashCode() : 0);
    result = 31 * result + trialverseId.hashCode();
    return result;
  }
}

package org.drugis.addis.analyses;

/**
 * Created by connor on 3/11/14.
 */
public class AnalysisCommand {
  private Integer projectId;
  private String name;
  private String type;

  public AnalysisCommand() {
  }

  public AnalysisCommand(Integer projectId, String name, String type) {
    this.projectId = projectId;
    this.name = name;
    this.type = type;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof AnalysisCommand)) return false;

    AnalysisCommand that = (AnalysisCommand) o;

    if (!name.equals(that.name)) return false;
    if (!projectId.equals(that.projectId)) return false;
    if (!type.equals(that.type)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = projectId.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + type.hashCode();
    return result;
  }
}

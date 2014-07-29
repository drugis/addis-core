package org.drugis.addis.projects;

import org.apache.commons.lang.StringUtils;

/**
 * Created by daan on 3/5/14.
 */
public class ProjectCommand {
  private String name;
  private String description;
  private String namespaceUid;

  public ProjectCommand() {
  }

  public ProjectCommand(String name, String description, String namespaceUid) {
    this.name = name;
    this.description = description;
    this.namespaceUid = namespaceUid;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description == null ? StringUtils.EMPTY : description;
  }

  public String getNamespaceUid() {
    return namespaceUid;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ProjectCommand that = (ProjectCommand) o;

    if (description != null ? !description.equals(that.description) : that.description != null) return false;
    if (!name.equals(that.name)) return false;
    if (!namespaceUid.equals(that.namespaceUid)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + (description != null ? description.hashCode() : 0);
    result = 31 * result + namespaceUid.hashCode();
    return result;
  }
}

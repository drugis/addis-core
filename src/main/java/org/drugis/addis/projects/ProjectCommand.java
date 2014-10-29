package org.drugis.addis.projects;

/**
 * Created by daan on 3/5/14.
 */
public class ProjectCommand {
  private String name;
  private String description;
  private String namespaceUid;
  private String datasetVersion;

  public ProjectCommand() {
  }

  public ProjectCommand(String name, String namespaceUid, String datasetVersion) {
    this.name = name;
    this.namespaceUid = namespaceUid;
    this.datasetVersion = datasetVersion;
  }

  public ProjectCommand(String name, String description, String namespaceUid, String datasetVersion) {
    this.name = name;
    this.description = description;
    this.namespaceUid = namespaceUid;
    this.datasetVersion = datasetVersion;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getNamespaceUid() {
    return namespaceUid;
  }

  public String getDatasetVersion() {
    return datasetVersion;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ProjectCommand that = (ProjectCommand) o;

    if (!datasetVersion.equals(that.datasetVersion)) return false;
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
    result = 31 * result + datasetVersion.hashCode();
    return result;
  }
}

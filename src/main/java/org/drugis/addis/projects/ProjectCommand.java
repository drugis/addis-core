package org.drugis.addis.projects;

import java.net.URI;

/**
 * Created by daan on 3/5/14.
 */
public class ProjectCommand {
  private String name;
  private String description;
  private String namespaceUid;
  private URI datasetVersion;

  public ProjectCommand() {
  }

  public ProjectCommand(String name, String namespaceUid, URI datasetVersion) {
    this.name = name;
    this.namespaceUid = namespaceUid;
    this.datasetVersion = datasetVersion;
  }

  public ProjectCommand(String name, String description, String namespaceUid, URI datasetVersion) {
    this.name = name;
    this.description = description;
    this.namespaceUid = namespaceUid;
    this.datasetVersion = datasetVersion;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public String getNamespaceUid() {
    return namespaceUid;
  }

  public URI getDatasetVersion() {
    return datasetVersion;
  }

  public void setDatasetVersion(URI datasetVersion) {
    this.datasetVersion = datasetVersion;
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

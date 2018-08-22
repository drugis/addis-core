package org.drugis.addis.projects;

import java.net.URI;
import java.util.Objects;

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
    return Objects.equals(name, that.name) &&
            Objects.equals(description, that.description) &&
            Objects.equals(namespaceUid, that.namespaceUid) &&
            Objects.equals(datasetVersion, that.datasetVersion);
  }

  @Override
  public int hashCode() {

    return Objects.hash(name, description, namespaceUid, datasetVersion);
  }
}

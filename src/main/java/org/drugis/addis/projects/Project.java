package org.drugis.addis.projects;

/**
 * Created by daan on 2/6/14.
 */
public class Project {
  private int ownerId;
  private int id;
  private String name;
  private String description;

  public Project() {
  }

  public Project(int ownerId, int id, String name, String description) {
    this.ownerId = ownerId;
    this.id = id;
    this.name = name;
    this.description = description;
  }

  public int getOwnerId() {
    return ownerId;
  }

  public void setOwnerId(int ownerId) {
    this.ownerId = ownerId;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
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

  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Project project = (Project) o;

    if (id != project.id) return false;
    if (ownerId != project.ownerId) return false;
    if (description != null ? !description.equals(project.description) : project.description != null) return false;
    if (!name.equals(project.name)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = ownerId;
    result = 31 * result + id;
    result = 31 * result + name.hashCode();
    result = 31 * result + (description != null ? description.hashCode() : 0);
    return result;
  }
}

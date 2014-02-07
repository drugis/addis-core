package org.drugis.addis.projects;

/**
 * Created by daan on 2/6/14.
 */
public class Project {
  private int owner;
  private int id;
  private String name;
  private String description;

  public Project() {
  }

  public Project(int id, int owner, String name, String description) {
    this.id = id;
    this.owner = owner;
    this.name = name;
    this.description = description;
  }

  public int getId() {
    return id;
  }

  public int getOwner() {
    return owner;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Project project = (Project) o;

    if (id != project.id) return false;
    if (owner != project.owner) return false;
    if (description != null ? !description.equals(project.description) : project.description != null) return false;
    if (!name.equals(project.name)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = owner;
    result = 31 * result + id;
    result = 31 * result + name.hashCode();
    result = 31 * result + (description != null ? description.hashCode() : 0);
    return result;
  }
}

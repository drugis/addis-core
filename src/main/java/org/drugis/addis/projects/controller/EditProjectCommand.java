package org.drugis.addis.projects.controller;

/**
 * Created by connor on 24-6-16.
 */
public class EditProjectCommand {
  private String name;
  private String description;

  public EditProjectCommand() {
  }

  public EditProjectCommand(String name, String description) {
    this.name = name;
    this.description = description;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    EditProjectCommand that = (EditProjectCommand) o;

    if (!name.equals(that.name)) return false;
    return description != null ? description.equals(that.description) : that.description == null;

  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + (description != null ? description.hashCode() : 0);
    return result;
  }
}

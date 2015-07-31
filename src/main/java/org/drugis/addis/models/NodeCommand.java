package org.drugis.addis.models;

/**
 * Created by daan on 30-7-15.
 */
public class NodeCommand {
  private int id;
  private String name;

  public NodeCommand() {
  }

  public NodeCommand(int id, String name) {
    this.id = id;
    this.name = name;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    NodeCommand that = (NodeCommand) o;

    if (id != that.id) return false;
    return name.equals(that.name);

  }

  @Override
  public int hashCode() {
    int result = id;
    result = 31 * result + name.hashCode();
    return result;
  }
}

package org.drugis.addis.models;

/**
 * Created by connor on 7/3/15.
 */
public class DetailsCommand {
  private NodeCommand from;
  private NodeCommand to;

  public DetailsCommand() {
  }

  public DetailsCommand(NodeCommand from, NodeCommand to) {
    this.from = from;
    this.to = to;
  }

  public NodeCommand getFrom() {
    return from;
  }

  public NodeCommand getTo() {
    return to;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DetailsCommand that = (DetailsCommand) o;

    if (!from.equals(that.from)) return false;
    return to.equals(that.to);

  }

  @Override
  public int hashCode() {
    int result = from.hashCode();
    result = 31 * result + to.hashCode();
    return result;
  }
}

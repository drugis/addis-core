package org.drugis.addis.models;

/**
 * Created by connor on 7/3/15.
 */
public class ModelTypeCommand {
  private String type;
  private DetailsCommand details;

  public ModelTypeCommand() {
  }

  public ModelTypeCommand(String type) {
    this.type = type;
  }

  public ModelTypeCommand(String type, DetailsCommand details) {
    this.type = type;
    this.details = details;
  }

  public String getType() {
    return type;
  }

  public DetailsCommand getDetails() {
    return details;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ModelTypeCommand that = (ModelTypeCommand) o;

    if (!type.equals(that.type)) return false;
    return !(details != null ? !details.equals(that.details) : that.details != null);

  }

  @Override
  public int hashCode() {
    int result = type.hashCode();
    result = 31 * result + (details != null ? details.hashCode() : 0);
    return result;
  }
}

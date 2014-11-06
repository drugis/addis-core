package org.drugis.trialverse.dataset.controller.command;

/**
 * Created by connor on 6-11-14.
 */
public class DatasetCommand {

  private String title;
  private String description;

  public DatasetCommand() {
  }

  public DatasetCommand(String title) {
    this.title = title;
  }

  public DatasetCommand(String title, String description) {
    this.title = title;
    this.description = description;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DatasetCommand that = (DatasetCommand) o;

    if (description != null ? !description.equals(that.description) : that.description != null) return false;
    if (!title.equals(that.title)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = title.hashCode();
    result = 31 * result + (description != null ? description.hashCode() : 0);
    return result;
  }
}

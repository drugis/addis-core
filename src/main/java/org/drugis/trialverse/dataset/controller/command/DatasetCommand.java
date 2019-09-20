package org.drugis.trialverse.dataset.controller.command;

import java.util.Objects;

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
    return Objects.equals(title, that.title) &&
            Objects.equals(description, that.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(title, description);
  }
}

package org.drugis.trialverse.dataset.controller;

import java.util.Objects;

public class DatasetArchiveCommand {
  private Boolean archived;

  public DatasetArchiveCommand() {
  }

  public DatasetArchiveCommand(Boolean archived) {
    this.archived = archived;
  }

  public Boolean getArchived() {
    return archived;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DatasetArchiveCommand that = (DatasetArchiveCommand) o;
    return Objects.equals(archived, that.archived);
  }

  @Override
  public int hashCode() {
    return Objects.hash(archived);
  }
}

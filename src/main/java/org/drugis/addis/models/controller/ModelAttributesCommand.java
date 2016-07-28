package org.drugis.addis.models.controller;

/**
 * Created by connor on 28-7-16.
 */
public class ModelAttributesCommand {
  private Boolean archived;

  public ModelAttributesCommand() {
  }

  public ModelAttributesCommand(Boolean archived) {
    this.archived = archived;
  }

  public Boolean getArchived() {
    return archived;
  }
}

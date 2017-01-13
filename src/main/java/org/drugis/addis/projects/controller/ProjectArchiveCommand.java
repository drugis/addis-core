package org.drugis.addis.projects.controller;

/**
 * Created by daan on 14-12-16.
 */
public class ProjectArchiveCommand {
  private Boolean isArchived;

  public ProjectArchiveCommand() {
  }

  public ProjectArchiveCommand(Boolean isArchived){
    this.isArchived = isArchived;
  }

  public Boolean getIsArchived() {
    return isArchived;
  }
}

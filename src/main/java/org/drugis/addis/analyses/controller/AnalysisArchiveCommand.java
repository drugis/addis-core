package org.drugis.addis.analyses.controller;

/**
 * Created by joris on 9-1-17.
 */
public class AnalysisArchiveCommand {
  private Boolean isArchived;

  public AnalysisArchiveCommand() {
  }

  public AnalysisArchiveCommand(Boolean isArchived){
    this.isArchived = isArchived;
  }

  public Boolean getIsArchived() {
    return isArchived;
  }
}

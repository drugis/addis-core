package org.drugis.trialverse.dataset.model;

import java.util.Date;

public class VersionNodeBuilder {
  private String uri;
  private String versionTitle;
  private Date versionDate;
  private String description;
  private String creator;
  private int historyOrder;
  private String applicationName;
  private String userHash;

  public VersionNodeBuilder setUri(String uri) {
    this.uri = uri;
    return this;
  }

  public VersionNodeBuilder setVersionTitle(String versionTitle) {
    this.versionTitle = versionTitle;
    return this;
  }

  public VersionNodeBuilder setVersionDate(Date versionDate) {
    this.versionDate = versionDate;
    return this;
  }

  public VersionNodeBuilder setDescription(String description) {
    this.description = description;
    return this;
  }

  public VersionNodeBuilder setCreator(String creator) {
    this.creator = creator;
    return this;
  }

  public VersionNodeBuilder setHistoryOrder(int historyOrder) {
    this.historyOrder = historyOrder;
    return this;
  }

  public VersionNodeBuilder setApplicationName(String applicationName) {
    this.applicationName = applicationName;
    return this;
  }

  public VersionNode build() {
    return new VersionNode(uri, versionTitle, versionDate, description, creator, userHash, historyOrder, applicationName);
  }

  public VersionNodeBuilder setUserHash(String userHash) {
    this.userHash = userHash;
    return this;
  }
}
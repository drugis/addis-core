package org.drugis.trialverse.dataset.model;

import java.net.URI;
import java.util.Date;

public class AdvancedVersionNodeBuilder {
  private String uri;
  private String versionTitle;
  private Date versionDate;
  private String description;
  private String creator;
  private Integer historyOrder;
  private String applicationName;
  private Integer userId;

  //Advanced Version Node data
  private URI graphUri;
  private URI graphRevision;

  public AdvancedVersionNodeBuilder setUri(String uri) {
    this.uri = uri;
    return this;
  }

  public AdvancedVersionNodeBuilder setVersionTitle(String versionTitle) {
    this.versionTitle = versionTitle;
    return this;
  }

  public AdvancedVersionNodeBuilder setVersionDate(Date versionDate) {
    this.versionDate = versionDate;
    return this;
  }

  public AdvancedVersionNodeBuilder setDescription(String description) {
    this.description = description;
    return this;
  }

  public AdvancedVersionNodeBuilder setCreator(String creator) {
    this.creator = creator;
    return this;
  }

  public AdvancedVersionNodeBuilder setHistoryOrder(int historyOrder) {
    this.historyOrder = historyOrder;
    return this;
  }

  public AdvancedVersionNodeBuilder setApplicationName(String applicationName) {
    this.applicationName = applicationName;
    return this;
  }

  public AdvancedVersionNode build() {
    return new AdvancedVersionNode(uri, versionTitle, versionDate, description, creator, userId, historyOrder, applicationName);
  }

  public AdvancedVersionNodeBuilder setUserId(Integer userId) {
    this.userId = userId;
    return this;
  }
  public AdvancedVersionNodeBuilder setGraphUri(URI graphUri) {
    this.graphUri = graphUri;
    return this;
  }
  public AdvancedVersionNodeBuilder setGraphRevision(URI graphRevision) {
    this.graphRevision = graphRevision;
    return this;
  }

}
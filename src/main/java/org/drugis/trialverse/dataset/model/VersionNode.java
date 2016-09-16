package org.drugis.trialverse.dataset.model;

import java.util.Date;

/**
 * Created by daan on 1-9-15.
 */
public class VersionNode {
  private String uri;
  private String versionTitle;
  private String creator;
  private Integer userId;
  private Integer historyOrder;
  private String applicationName;
  private Merge merge;
  private Date versionDate;
  private String description;

  public VersionNode() {
  }

  public VersionNode(String uri, String versionTitle, Date versionDate, String description, String creator, Integer userId, Integer historyOrder, String applicationName) {
    this.uri = uri;
    this.versionTitle = versionTitle;
    this.versionDate = versionDate;
    this.description = description;
    this.creator = creator;
    this.userId = userId;
    this.historyOrder = historyOrder;
    this.applicationName = applicationName;
  }

  public void setMerge(Merge merge) {
    this.merge = merge;
  }

  public Merge getMerge() {
    return merge;
  }

  public String getUri() {
    return uri;
  }

  public String getVersionTitle() {
    return versionTitle;
  }

  public String getCreator() {
    return creator;
  }

  public Integer getHistoryOrder() {
    return historyOrder;
  }

  public String getDescription() {
    return description;
  }

  public Date getVersionDate() {
    return versionDate;
  }

  public String getApplicationName() {
    return applicationName;
  }

  public Integer getUserId() {
    return userId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    VersionNode that = (VersionNode) o;

    if (uri != null ? !uri.equals(that.uri) : that.uri != null) return false;
    if (versionTitle != null ? !versionTitle.equals(that.versionTitle) : that.versionTitle != null) return false;
    if (creator != null ? !creator.equals(that.creator) : that.creator != null) return false;
    if (userId != null ? !userId.equals(that.userId) : that.userId != null) return false;
    if (historyOrder != null ? !historyOrder.equals(that.historyOrder) : that.historyOrder != null) return false;
    if (applicationName != null ? !applicationName.equals(that.applicationName) : that.applicationName != null)
      return false;
    if (merge != null ? !merge.equals(that.merge) : that.merge != null) return false;
    if (versionDate != null ? !versionDate.equals(that.versionDate) : that.versionDate != null) return false;
    return description != null ? description.equals(that.description) : that.description == null;

  }

  @Override
  public int hashCode() {
    int result = uri != null ? uri.hashCode() : 0;
    result = 31 * result + (versionTitle != null ? versionTitle.hashCode() : 0);
    result = 31 * result + (creator != null ? creator.hashCode() : 0);
    result = 31 * result + (userId != null ? userId.hashCode() : 0);
    result = 31 * result + (historyOrder != null ? historyOrder.hashCode() : 0);
    result = 31 * result + (applicationName != null ? applicationName.hashCode() : 0);
    result = 31 * result + (merge != null ? merge.hashCode() : 0);
    result = 31 * result + (versionDate != null ? versionDate.hashCode() : 0);
    result = 31 * result + (description != null ? description.hashCode() : 0);
    return result;
  }
}

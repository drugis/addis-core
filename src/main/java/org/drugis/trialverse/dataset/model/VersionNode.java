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
  private int historyOrder;
  private String applicationName;
  private Merge merge;
  private Date versionDate;
  private String description;

  public VersionNode() {
  }

  public VersionNode(String uri, String versionTitle, Date versionDate, String description, String creator, int userId, int historyOrder, String applicationName) {
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

  public int getHistoryOrder() {
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

    if (historyOrder != that.historyOrder) return false;
    if (!uri.equals(that.uri)) return false;
    if (!versionTitle.equals(that.versionTitle)) return false;
    if (!creator.equals(that.creator)) return false;
    if (!userId.equals(that.userId)) return false;
    if (applicationName != null ? !applicationName.equals(that.applicationName) : that.applicationName != null)
      return false;
    if (merge != null ? !merge.equals(that.merge) : that.merge != null) return false;
    if (!versionDate.equals(that.versionDate)) return false;
    return !(description != null ? !description.equals(that.description) : that.description != null);

  }

  @Override
  public int hashCode() {
    int result = uri.hashCode();
    result = 31 * result + versionTitle.hashCode();
    result = 31 * result + creator.hashCode();
    result = 31 * result + userId.hashCode();
    result = 31 * result + historyOrder;
    result = 31 * result + (applicationName != null ? applicationName.hashCode() : 0);
    result = 31 * result + (merge != null ? merge.hashCode() : 0);
    result = 31 * result + versionDate.hashCode();
    result = 31 * result + (description != null ? description.hashCode() : 0);
    return result;
  }
}

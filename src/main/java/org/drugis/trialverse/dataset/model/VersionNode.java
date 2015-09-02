package org.drugis.trialverse.dataset.model;

/**
 * Created by daan on 1-9-15.
 */
public class VersionNode {
  private String uri;
  private String versionTitle;
  private String creator;
  private int i;
  private Merge merge;

  public VersionNode() {
  }

  public VersionNode(String uri, String versionTitle, String creator, int i) {
    this.uri = uri;
    this.versionTitle = versionTitle;
    this.creator = creator;
    this.i = i;
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

  public int getI() {
    return i;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    VersionNode that = (VersionNode) o;

    if (i != that.i) return false;
    if (!uri.equals(that.uri)) return false;
    if (!versionTitle.equals(that.versionTitle)) return false;
    if (!creator.equals(that.creator)) return false;
    return !(merge != null ? !merge.equals(that.merge) : that.merge != null);

  }

  @Override
  public int hashCode() {
    int result = uri.hashCode();
    result = 31 * result + versionTitle.hashCode();
    result = 31 * result + creator.hashCode();
    result = 31 * result + i;
    result = 31 * result + (merge != null ? merge.hashCode() : 0);
    return result;
  }
}

package org.drugis.trialverse.search.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.codec.digest.DigestUtils;
import org.drugis.addis.security.Account;
import org.drugis.trialverse.dataset.model.VersionMapping;

/**
 * Created by connor on 9/4/15.
 */
@JsonDeserialize(using = SearchResultDeserialiser.class)
public class SearchResult {
  private String graphUri;
  private String study;
  private String title;
  private String comment;
  private String versionUuid;

  private Integer owner;
  private String ownerUuid;
  private String datasetUrl;

  public SearchResult(String graphUri, String study, String title, String comment, String versionUuid) {
    this.graphUri = graphUri;
    this.study = study;
    this.title = title;
    this.comment = comment;
    this.versionUuid = versionUuid;
  }

  public String getGraphUri() {
    return graphUri;
  }

  public String getStudy() {
    return study;
  }

  public String getTitle() {
    return title;
  }

  public String getComment() {
    return comment;
  }

  public Integer getOwner() {
    return owner;
  }

  public String getDatasetUrl() {
    return datasetUrl;
  }

  public String getOwnerUuid() {
    return ownerUuid;
  }

  public String getVersionUuid() {
    return versionUuid;
  }

  public void setOwner(Integer owner) {
    this.owner = owner;
  }

  public void setOwnerUuid(String ownerUuid) {
    this.ownerUuid = ownerUuid;
  }
  public void setDatasetUrl(String datasetUrl) {
    this.datasetUrl = datasetUrl;
  }

  public void addMetaData(VersionMapping mapping, Account account) {
    this.setDatasetUrl(mapping.getTrialverseDatasetUrl());
    this.setOwner(account.getId());
    this.setOwnerUuid(DigestUtils.sha256Hex(mapping.getOwnerUuid()));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SearchResult that = (SearchResult) o;

    if (!graphUri.equals(that.graphUri)) return false;
    if (!study.equals(that.study)) return false;
    if (!title.equals(that.title)) return false;
    if (comment != null ? !comment.equals(that.comment) : that.comment != null) return false;
    if (!versionUuid.equals(that.versionUuid)) return false;
    if (owner != null ? !owner.equals(that.owner) : that.owner != null) return false;
    if (ownerUuid != null ? !ownerUuid.equals(that.ownerUuid) : that.ownerUuid != null) return false;
    return !(datasetUrl != null ? !datasetUrl.equals(that.datasetUrl) : that.datasetUrl != null);

  }

  @Override
  public int hashCode() {
    int result = graphUri.hashCode();
    result = 31 * result + study.hashCode();
    result = 31 * result + title.hashCode();
    result = 31 * result + (comment != null ? comment.hashCode() : 0);
    result = 31 * result + versionUuid.hashCode();
    result = 31 * result + (owner != null ? owner.hashCode() : 0);
    result = 31 * result + (ownerUuid != null ? ownerUuid.hashCode() : 0);
    result = 31 * result + (datasetUrl != null ? datasetUrl.hashCode() : 0);
    return result;
  }
}

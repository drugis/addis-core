package org.drugis.trialverse.search.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.codec.digest.DigestUtils;
import org.drugis.trialverse.dataset.model.VersionMapping;

/**
 * Created by connor on 9/4/15.
 */
@JsonDeserialize(using = SearchResultDeserialiser.class)
public class SearchResult {
  private String study;
  private String title;
  private String comment;

  private String owner;
  private String ownerUuid;
  private String datasetUrl;

  public SearchResult() {
  }

  public SearchResult(String study, String title, String comment) {
    this.study = study;
    this.title = title;
    this.comment = comment;
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

  public String getOwner() {
    return owner;
  }

  public String getDatasetUrl() {
    return datasetUrl;
  }

  public String getOwnerUuid() {
    return ownerUuid;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public void setOwnerUuid(String ownerUuid) {
    this.ownerUuid = ownerUuid;
  }
  public void setDatasetUrl(String datasetUrl) {
    this.datasetUrl = datasetUrl;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SearchResult that = (SearchResult) o;

    if (!study.equals(that.study)) return false;
    if (!title.equals(that.title)) return false;
    if (comment != null ? !comment.equals(that.comment) : that.comment != null) return false;
    if (owner != null ? !owner.equals(that.owner) : that.owner != null) return false;
    if (ownerUuid != null ? !ownerUuid.equals(that.ownerUuid) : that.ownerUuid != null) return false;
    return !(datasetUrl != null ? !datasetUrl.equals(that.datasetUrl) : that.datasetUrl != null);

  }

  @Override
  public int hashCode() {
    int result = study.hashCode();
    result = 31 * result + title.hashCode();
    result = 31 * result + (comment != null ? comment.hashCode() : 0);
    result = 31 * result + (owner != null ? owner.hashCode() : 0);
    result = 31 * result + (ownerUuid != null ? ownerUuid.hashCode() : 0);
    result = 31 * result + (datasetUrl != null ? datasetUrl.hashCode() : 0);
    return result;
  }

  public void addMetaData(VersionMapping mapping) {
    this.setDatasetUrl(mapping.getTrialverseDatasetUrl());
    this.setOwner(mapping.getOwnerUuid());
    this.setOwnerUuid(DigestUtils.sha256Hex(mapping.getOwnerUuid()));
  }

}

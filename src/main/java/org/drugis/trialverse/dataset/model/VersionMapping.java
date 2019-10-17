package org.drugis.trialverse.dataset.model;

import javax.persistence.*;
import java.io.Serializable;
import java.net.URI;
import java.util.Objects;

@Entity
@Table
public class VersionMapping implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private String versionedDatasetUrl;
  private String ownerUuid;
  private String trialverseDatasetUrl;
  private Boolean archived;
  private String archivedOn;

  public VersionMapping() {
  }

  public VersionMapping(String versionedDatasetUrl, String ownerUuid, String trialverseDatasetUrl) {
    this.versionedDatasetUrl = versionedDatasetUrl;
    this.ownerUuid = ownerUuid;
    this.trialverseDatasetUrl = trialverseDatasetUrl;
  }

  public VersionMapping(
          Integer id,
          String versionedDatasetUrl,
          String ownerUuid,
          String trialverseDatasetUrl,
          Boolean archived,
          String archivedOn
  ) {
    this.id = id;
    this.versionedDatasetUrl = versionedDatasetUrl;
    this.ownerUuid = ownerUuid;
    this.trialverseDatasetUrl = trialverseDatasetUrl;
    this.archived = archived;
    this.archivedOn = archivedOn;
  }

  public VersionMapping(
          String versionedDatasetUrl,
          String ownerUuid,
          String trialverseDatasetUrl,
          Boolean archived,
          String archivedOn
  ) {
    this.versionedDatasetUrl = versionedDatasetUrl;
    this.ownerUuid = ownerUuid;
    this.trialverseDatasetUrl = trialverseDatasetUrl;
    this.archived = archived;
    this.archivedOn = archivedOn;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getVersionedDatasetUrl() {
    return versionedDatasetUrl;
  }

  public URI getVersionedDatasetUri() {
    return URI.create(versionedDatasetUrl);
  }

  public URI getTrialverseDatasetUri() {
    return URI.create(trialverseDatasetUrl);
  }

  public String getOwnerUuid() {
    return ownerUuid;
  }

  public String getTrialverseDatasetUrl() {
    return trialverseDatasetUrl;
  }

  public Boolean getArchived() {
    return archived;
  }

  public String getArchivedOn() {
    return archivedOn;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    VersionMapping that = (VersionMapping) o;
    return Objects.equals(id, that.id) &&
            Objects.equals(versionedDatasetUrl, that.versionedDatasetUrl) &&
            Objects.equals(ownerUuid, that.ownerUuid) &&
            Objects.equals(trialverseDatasetUrl, that.trialverseDatasetUrl) &&
            Objects.equals(archived, that.archived) &&
            Objects.equals(archivedOn, that.archivedOn);
  }

  @Override
  public int hashCode() {

    return Objects.hash(id, versionedDatasetUrl, ownerUuid, trialverseDatasetUrl, archived, archivedOn);
  }
}

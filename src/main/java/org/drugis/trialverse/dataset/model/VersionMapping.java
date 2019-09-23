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

  private String versionUrl;
  private String ownerUuid;
  private String datasetUrl;
  private Boolean archived;
  private String archivedOn;

  public VersionMapping() {
  }

  public VersionMapping(String versionUrl, String ownerUuid, String datasetUrl) {
    this.versionUrl = versionUrl;
    this.ownerUuid = ownerUuid;
    this.datasetUrl = datasetUrl;
  }

  public VersionMapping(
          Integer id,
          String versionUrl,
          String ownerUuid,
          String datasetUrl,
          Boolean archived,
          String archivedOn
  ) {
    this.id = id;
    this.versionUrl = versionUrl;
    this.ownerUuid = ownerUuid;
    this.datasetUrl = datasetUrl;
    this.archived = archived;
    this.archivedOn = archivedOn;
  }

  public VersionMapping(
          String versionUrl,
          String ownerUuid,
          String datasetUrl,
          Boolean archived,
          String archivedOn
  ) {
    this.versionUrl = versionUrl;
    this.ownerUuid = ownerUuid;
    this.datasetUrl = datasetUrl;
    this.archived = archived;
    this.archivedOn = archivedOn;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getVersionUrl() {
    return versionUrl;
  }

  public URI getVersionedDatasetUri() {
    return URI.create(versionUrl);
  }

  public URI getTrialverseDatasetUri() {
    return URI.create(datasetUrl);
  }

  public String getOwnerUuid() {
    return ownerUuid;
  }

  public String getDatasetUrl() {
    return datasetUrl;
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
            Objects.equals(versionUrl, that.versionUrl) &&
            Objects.equals(ownerUuid, that.ownerUuid) &&
            Objects.equals(datasetUrl, that.datasetUrl) &&
            Objects.equals(archived, that.archived) &&
            Objects.equals(archivedOn, that.archivedOn);
  }

  @Override
  public int hashCode() {

    return Objects.hash(id, versionUrl, ownerUuid, datasetUrl, archived, archivedOn);
  }
}

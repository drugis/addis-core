package org.drugis.addis.trialverse.model.mapping;

/**
 * Created by daan on 12-2-16.
 */
public class VersionedUuidAndOwner {
  private String versionedUuid;
  private Integer ownerId;

  public VersionedUuidAndOwner(String versionedUuid, Integer ownerId) {
    this.versionedUuid = versionedUuid;
    this.ownerId = ownerId;
  }

  public String getTriplestoreUri() {
    return versionedUuid;
  }

  public Integer getOwnerId() {
    return ownerId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    VersionedUuidAndOwner that = (VersionedUuidAndOwner) o;

    if (!versionedUuid.equals(that.versionedUuid)) return false;
    return ownerId.equals(that.ownerId);

  }

  @Override
  public int hashCode() {
    int result = versionedUuid.hashCode();
    result = 31 * result + ownerId.hashCode();
    return result;
  }
}

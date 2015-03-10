package org.drugis.trialverse.dataset.model;

import javax.persistence.*;

/**
 * Created by connor on 10-3-15.
 */
@Entity
@Table
public class VersionMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String datasetUuid;
    private String ownerUuid;
    private String versionKey;

    public VersionMapping(Integer id, String datasetUuid, String ownerUuid, String versionKey) {
        this.id = id;
        this.datasetUuid = datasetUuid;
        this.ownerUuid = ownerUuid;
        this.versionKey = versionKey;
    }

    public VersionMapping(String datasetUuid, String ownerUuid, String versionKey) {
        this.datasetUuid = datasetUuid;
        this.ownerUuid = ownerUuid;
        this.versionKey = versionKey;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDatasetUuid() {
        return datasetUuid;
    }

    public void setDatasetUuid(String datasetUuid) {
        this.datasetUuid = datasetUuid;
    }

    public String getOwnerUuid() {
        return ownerUuid;
    }

    public void setOwnerUuid(String ownerUuid) {
        this.ownerUuid = ownerUuid;
    }

    public String getVersionKey() {
        return versionKey;
    }

    public void setVersionKey(String versionKey) {
        this.versionKey = versionKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VersionMapping that = (VersionMapping) o;

        if (!datasetUuid.equals(that.datasetUuid)) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (!ownerUuid.equals(that.ownerUuid)) return false;
        if (!versionKey.equals(that.versionKey)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + datasetUuid.hashCode();
        result = 31 * result + ownerUuid.hashCode();
        result = 31 * result + versionKey.hashCode();
        return result;
    }
}

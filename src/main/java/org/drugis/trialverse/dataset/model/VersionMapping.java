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

    private String datasetLocation;
    private String ownerUuid;
    private String trialverseDataset;

    public VersionMapping(Integer id, String datasetLocation, String ownerUuid, String trialverseDataset) {
        this.id = id;
        this.datasetLocation = datasetLocation;
        this.ownerUuid = ownerUuid;
        this.trialverseDataset = trialverseDataset;
    }

    public VersionMapping(String datasetLocation, String ownerUuid, String trialverseDataset) {
        this.datasetLocation = datasetLocation;
        this.ownerUuid = ownerUuid;
        this.trialverseDataset = trialverseDataset;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDatasetLocation() {
        return datasetLocation;
    }

    public void setDatasetLocation(String datasetLocation) {
        this.datasetLocation = datasetLocation;
    }

    public String getOwnerUuid() {
        return ownerUuid;
    }

    public void setOwnerUuid(String ownerUuid) {
        this.ownerUuid = ownerUuid;
    }

    public String getTrialverseDataset() {
        return trialverseDataset;
    }

    public void setTrialverseDataset(String trialverseDataset) {
        this.trialverseDataset = trialverseDataset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VersionMapping that = (VersionMapping) o;

        if (!datasetLocation.equals(that.datasetLocation)) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (!ownerUuid.equals(that.ownerUuid)) return false;
        if (!trialverseDataset.equals(that.trialverseDataset)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + datasetLocation.hashCode();
        result = 31 * result + ownerUuid.hashCode();
        result = 31 * result + trialverseDataset.hashCode();
        return result;
    }
}

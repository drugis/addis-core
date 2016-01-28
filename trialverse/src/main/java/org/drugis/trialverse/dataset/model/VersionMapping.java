package org.drugis.trialverse.dataset.model;

import javax.persistence.*;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by connor on 10-3-15.
 */
@Entity
@Table
public class VersionMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String versionedDatasetUrl;
    private String ownerUuid;
    private String trialverseDatasetUrl;

    public VersionMapping(Integer id, String versionedDatasetUrl, String ownerUuid, String trialverseDatasetUrl) {
        this.id = id;
        this.versionedDatasetUrl = versionedDatasetUrl;
        this.ownerUuid = ownerUuid;
        this.trialverseDatasetUrl = trialverseDatasetUrl;
    }

    public VersionMapping(String versionedDatasetUrl, String ownerUuid, String trialverseDatasetUrl) {
        this.versionedDatasetUrl = versionedDatasetUrl;
        this.ownerUuid = ownerUuid;
        this.trialverseDatasetUrl = trialverseDatasetUrl;
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

    public URI getVersionedDatasetUri() throws URISyntaxException {
        return new URI(versionedDatasetUrl);
    }

    public String getOwnerUuid() {
        return ownerUuid;
    }

    public String getTrialverseDatasetUrl() {
        return trialverseDatasetUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VersionMapping that = (VersionMapping) o;

        if (!versionedDatasetUrl.equals(that.versionedDatasetUrl)) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (!ownerUuid.equals(that.ownerUuid)) return false;
        if (!trialverseDatasetUrl.equals(that.trialverseDatasetUrl)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + versionedDatasetUrl.hashCode();
        result = 31 * result + ownerUuid.hashCode();
        result = 31 * result + trialverseDatasetUrl.hashCode();
        return result;
    }
}

package org.drugis.trialverse.dataset.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Created by connor on 11-3-16.
 */
@Entity
@Table
public class FeaturedDataset implements Serializable {

  @Id
  private String datasetUrl;

  public FeaturedDataset() {
  }

  public FeaturedDataset(String datasetUrl) {
    this.datasetUrl = datasetUrl;
  }

  public String getDatasetUrl() {
    return datasetUrl;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    FeaturedDataset that = (FeaturedDataset) o;

    return datasetUrl != null ? datasetUrl.equals(that.datasetUrl) : that.datasetUrl == null;

  }

  @Override
  public int hashCode() {
    return datasetUrl != null ? datasetUrl.hashCode() : 0;
  }
}

package org.drugis.trialverse.dataset.repository;

import org.drugis.trialverse.dataset.model.FeaturedDataset;

import java.util.List;

/**
 * Created by connor on 11-3-16.
 */
public interface FeaturedDatasetRepository {
  List<FeaturedDataset> findAll();
}

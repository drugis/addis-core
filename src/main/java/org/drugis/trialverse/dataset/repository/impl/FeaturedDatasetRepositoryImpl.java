package org.drugis.trialverse.dataset.repository.impl;

import org.drugis.trialverse.dataset.model.FeaturedDataset;
import org.drugis.trialverse.dataset.repository.FeaturedDatasetRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * Created by connor on 11-3-16.
 */
@Repository
public class FeaturedDatasetRepositoryImpl implements FeaturedDatasetRepository {

  @Qualifier("emAddisCore")
  @PersistenceContext(unitName = "addisCore")
  private EntityManager em;

  @Override
  @Cacheable(cacheNames = "featuredDatasets")
  public List<FeaturedDataset> findAll() {
    return em.createQuery("FROM FeaturedDataset").getResultList();
  }
}

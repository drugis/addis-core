package org.drugis.addis.ordering.repository.impl;

import org.drugis.addis.ordering.Ordering;
import org.drugis.addis.ordering.repository.OrderingRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class OrderingRepositoryImpl implements OrderingRepository {
  @Qualifier("emAddisCore")
  @PersistenceContext(unitName = "addisCore")
  private EntityManager em;

  @Override
  public String get(Integer analysisId) {
    Ordering ordering = em.find(Ordering.class, analysisId);
    return ordering.getOrdering();
  }

}

package org.drugis.addis.ordering.repository;

import org.drugis.addis.ordering.Ordering;

public interface OrderingRepository {
  Ordering get(Integer analysisId);

  void put(Integer analysisId, String[] criteria, String[] alternatives);
}

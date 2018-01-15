package org.drugis.addis.toggledColumns.repository;

import org.drugis.addis.toggledColumns.ToggledColumns;

public interface ToggledColumnsRepository {
  ToggledColumns get(Integer analysisId);

  void put(Integer analysisId, String ordering);
}

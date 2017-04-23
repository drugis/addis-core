package org.drugis.addis.scaledUnits.repository;

import org.drugis.addis.scaledUnits.ScaledUnit;

import java.net.URI;
import java.util.List;

/**
 * Created by joris on 19-4-17.
 */
public interface ScaledUnitRepository {
  List<ScaledUnit> query(Integer projectId) ;

  void create(Integer projectId, URI conceptUri, Double multiplier, String name);
}

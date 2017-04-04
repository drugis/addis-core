package org.drugis.addis.EffectsTable;

import org.apache.jena.atlas.lib.Pair;

/**
 * Created by joris on 4-4-17.
 */
public interface EffectsTableService {
  void setEffectsTable(Integer analysisId, Pair<Integer,Boolean> pair);
  EffectsTable getEffectsTable(Integer analysisId);
}

package org.drugis.addis.EffectsTable;

import org.apache.jena.atlas.lib.Pair;

/**
 * Created by joris on 4-4-17.
 */
public interface EffectsTableRepository {

  void setEffectsTable(Integer analysisId, Pair<Integer, Boolean> effectsTable);

  EffectsTable getEffectsTable(Integer analysisId);
}

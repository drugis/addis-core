package org.drugis.addis.effectsTables.repository;

import org.drugis.addis.effectsTables.EffectsTableExclusion;

import java.util.List;

/**
 * Created by joris on 4-4-17.
 */
public interface EffectsTableRepository {

  void setEffectsTableExclusion(Integer analysisId, Integer alternativeId);

  List<EffectsTableExclusion> getEffectsTableExclusions(Integer analysisId);
}

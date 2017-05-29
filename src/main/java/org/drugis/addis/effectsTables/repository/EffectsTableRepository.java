package org.drugis.addis.effectsTables.repository;

import org.drugis.addis.effectsTables.EffectsTableAlternativeInclusion;

import java.util.List;

/**
 * Created by joris on 4-4-17.
 */
public interface EffectsTableRepository {

  void setEffectsTableAlternativeInclusion(Integer analysisId, List<String> alternativeId);

  List<EffectsTableAlternativeInclusion> getEffectsTableAlternativeInclusions(Integer analysisId);
}

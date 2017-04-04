package org.drugis.addis.EffectsTable.impl;


import org.apache.jena.atlas.lib.Pair;
import org.drugis.addis.EffectsTable.EffectsTable;
import org.drugis.addis.EffectsTable.EffectsTableRepository;
import org.drugis.addis.EffectsTable.EffectsTableService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * Created by joris on 4-4-17.
 */
@Service
public class EffectsTableServiceImpl implements EffectsTableService {

  @Inject
  EffectsTableRepository effectsTableRepository;

  @Override
  public void setEffectsTable(Integer analysisId, Pair<Integer,Boolean> pair) {
    effectsTableRepository.setEffectsTable(analysisId,pair);
  }

  @Override
  public EffectsTable getEffectsTable(Integer analysisId) {
    return effectsTableRepository.getEffectsTable(analysisId);
  }
}

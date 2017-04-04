package org.drugis.addis.EffectsTable;

import org.apache.jena.atlas.lib.Pair;
import org.drugis.addis.EffectsTable.impl.EffectsTableServiceImpl;
import org.drugis.addis.base.AbstractAddisCoreController;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

/**
 * Created by joris on 4-4-17.
 */
@Controller
@Transactional("ptmAddisCore")
public class EffectsTableController extends AbstractAddisCoreController {

  @Inject
  EffectsTableServiceImpl effectsTableService;

  @RequestMapping(value = "/projects/{projectId}/analyses/{workspaceId}/effectsTable", method = RequestMethod.GET)
  @ResponseBody
  public EffectsTable getEffectsTable(@PathVariable Integer projectId, @PathVariable Integer analysisId){
    return effectsTableService.getEffectsTable(analysisId);
  }

  @RequestMapping(value="/projects/{projectId}/analyses/{workspaceId}/effectsTable", method = RequestMethod.POST)
  public void setEffectsTable(@PathVariable Integer projectId, @PathVariable Integer analysisId, @RequestBody Pair<Integer, Boolean> pair){
    effectsTableService.setEffectsTable(analysisId, pair);
  }
}

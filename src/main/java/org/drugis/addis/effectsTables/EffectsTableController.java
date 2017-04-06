package org.drugis.addis.effectsTables;

import org.drugis.addis.base.AbstractAddisCoreController;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by joris on 4-4-17.
 */
@Controller
@Transactional("ptmAddisCore")
public class EffectsTableController extends AbstractAddisCoreController {

  @Inject
  private EffectsTableRepository effectsTableRepository;

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}/effectsTable", method = RequestMethod.GET)
  @ResponseBody
  public List<EffectsTableExclusion> getEffectsTable(@PathVariable Integer projectId, @PathVariable Integer analysisId){
    return effectsTableRepository.getEffectsTableExclusions(analysisId);
  }

  @RequestMapping(value="/projects/{projectId}/analyses/{analysisId}/effectsTable", method = RequestMethod.POST)
  public void setEffectsTableExclusion(@PathVariable Integer projectId, @PathVariable Integer analysisId, @RequestBody Integer alternativeId){
    effectsTableRepository.setEffectsTableExclusion(analysisId, alternativeId);
  }
}

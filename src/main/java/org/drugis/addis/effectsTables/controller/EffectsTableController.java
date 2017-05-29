package org.drugis.addis.effectsTables.controller;

import org.apache.http.HttpStatus;
import org.drugis.addis.base.AbstractAddisCoreController;
import org.drugis.addis.effectsTables.AlternativeInclusionsCommand;
import org.drugis.addis.effectsTables.EffectsTableAlternativeInclusion;
import org.drugis.addis.effectsTables.repository.EffectsTableRepository;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.List;

/**
 * Created by joris on 4-4-17.
 */
@Controller
@Transactional("ptmAddisCore")
public class EffectsTableController extends AbstractAddisCoreController {

  @Inject
  private EffectsTableRepository effectsTableRepository;

  @Inject
  private AccountRepository accountRepository;

  @Inject
  private ProjectService projectService;

  @RequestMapping(value = "/projects/{projectId}/analyses/{analysisId}/effectsTable", method = RequestMethod.GET)
  @ResponseBody
  public List<EffectsTableAlternativeInclusion> getEffectsTable(@PathVariable Integer projectId, @PathVariable Integer analysisId){
    return effectsTableRepository.getEffectsTableAlternativeInclusions(analysisId);
  }

  @RequestMapping(value="/projects/{projectId}/analyses/{analysisId}/effectsTable", method = RequestMethod.POST)
  public void setEffectsTableInclusions(HttpServletResponse response, Principal currentUser, @PathVariable Integer projectId,
                                       @PathVariable Integer analysisId, @RequestBody AlternativeInclusionsCommand alternativeInclusionsCommand) throws ResourceDoesNotExistException, MethodNotAllowedException {
    Account user = accountRepository.findAccountByUsername(currentUser.getName());
    projectService.checkProjectExistsAndModifiable(user, projectId);
    effectsTableRepository.setEffectsTableAlternativeInclusion(analysisId, alternativeInclusionsCommand.getAlternativeIds());
    response.setStatus(HttpStatus.SC_OK);
  }
}

package org.drugis.addis.outcomes.controller;

import org.drugis.addis.base.AbstractAddisCoreController;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.outcomes.repository.OutcomeRepository;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Collection;

/**
 * Created by daan on 3/5/14.
 */
@Controller
@Transactional("ptmAddisCore")
public class OutcomeController extends AbstractAddisCoreController {

  @Inject
  private OutcomeRepository outcomeRepository;
  @Inject
  private AccountRepository accountRepository;

  @RequestMapping(value = "/projects/{projectId}/outcomes", method = RequestMethod.GET)
  @ResponseBody
  public Collection<Outcome> query(Principal currentUser, @PathVariable Integer projectId) throws MethodNotAllowedException {
    Account user = accountRepository.findAccountByUsername(currentUser.getName());
    if (user != null) {
      return outcomeRepository.query(projectId);
    } else {
      throw new MethodNotAllowedException();
    }
  }


}

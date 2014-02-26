package org.drugis.addis.trialverse.controller;

import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.trialverse.Trialverse;
import org.drugis.addis.trialverse.repository.TrialverseRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.security.Principal;
import java.util.Collection;

/**
 * Created by connor on 2/12/14.
 */
@Controller
public class TrialverseController {

  @Inject
  private AccountRepository accountRepository;

  @Inject
  private TrialverseRepository trialverseRepository;

  @RequestMapping(value = "/trialverse", method = RequestMethod.GET)
  @ResponseBody
  public Collection<Trialverse> query(Principal currentUser) throws MethodNotAllowedException {
    Account user = accountRepository.findAccountByUsername(currentUser.getName());
    if (user != null) {
      return trialverseRepository.query();
    } else {
      throw new MethodNotAllowedException();
    }
  }

  @RequestMapping(value = "/trialverse/{trialverseId}", method = RequestMethod.GET)
  @ResponseBody
  public Trialverse get(Principal currentUser, @PathVariable Integer trialverseId) throws MethodNotAllowedException {
    Account user = accountRepository.findAccountByUsername(currentUser.getName());
    if (user != null) {
      return trialverseRepository.get(trialverseId);
    } else {
      throw new MethodNotAllowedException();
    }
  }
}

package org.drugis.trialverse.user.controller;

import java.security.Principal;

import javax.inject.Inject;

import org.drugis.addis.base.AbstractAddisCoreController;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.trialverse.security.TrialversePrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/whoami")
public class WhoAmIController extends AbstractAddisCoreController {

  @Inject
  private AccountRepository accountRepository;

  Logger logger = LoggerFactory.getLogger(getClass());

  @RequestMapping(method = RequestMethod.GET)
  @ResponseBody
  public Account getSelf(Principal currentUser) {
    logger.trace("retrieving whoami");
    TrialversePrincipal trialversePrincipal = new TrialversePrincipal(currentUser);
    return accountRepository.findAccountByUsername(trialversePrincipal.getUserName());
  }
}

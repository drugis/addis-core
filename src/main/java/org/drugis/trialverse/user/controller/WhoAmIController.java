package org.drugis.trialverse.user.controller;

import org.apache.http.client.HttpClient;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.trialverse.security.TrialversePrincipal;
import org.drugis.trialverse.util.controller.AbstractTrialverseController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;

import java.security.Principal;
import java.util.List;

/**
 * Created by connor on 1-5-15.
 */
@Controller
@RequestMapping(value = "/whoami")
public class WhoAmIController extends AbstractTrialverseController {

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

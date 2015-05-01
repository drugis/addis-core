package org.drugis.trialverse.user.controller;

import org.apache.http.client.HttpClient;
import org.drugis.trialverse.security.Account;
import org.drugis.trialverse.security.repository.AccountRepository;
import org.drugis.trialverse.util.controller.AbstractTrialverseController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by connor on 1-5-15.
 */
@Controller
@RequestMapping(value = "/users")
public class UserController extends AbstractTrialverseController {

  @Inject
  private AccountRepository accountRepository;

  @Inject
  private HttpClient httpClient;

  private final static String JSON_TYPE = "application/json; charset=UTF-8";

  Logger logger = LoggerFactory.getLogger(getClass());

  @RequestMapping(value = "/{userHash}", method = RequestMethod.GET)
  @ResponseBody
  public Account getUser(HttpServletResponse httpServletResponse, @PathVariable String userHash) {
    logger.trace("retrieving user");
    return accountRepository.findAccountByHash(userHash);
  }
}

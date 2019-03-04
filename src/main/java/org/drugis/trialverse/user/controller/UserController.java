package org.drugis.trialverse.user.controller;

import org.apache.http.client.HttpClient;
import org.drugis.addis.base.AbstractAddisCoreController;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
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
@RequestMapping(value = "/users")
public class UserController extends AbstractAddisCoreController {

  @Inject
  private AccountRepository accountRepository;

  @Inject
  private HttpClient httpClient;

  private final static String JSON_TYPE = "application/json; charset=UTF-8";

  Logger logger = LoggerFactory.getLogger(getClass());

  @RequestMapping(value = "/{userId}", method = RequestMethod.GET)
  @ResponseBody
  public Account getUser(@PathVariable Integer userId) {
    logger.trace("retrieving user");
    return accountRepository.findAccountById(userId);
  }

  @RequestMapping(method = RequestMethod.GET)
  @ResponseBody
  public List<Account> getAllUsers() {
    logger.trace("retrieving all user");
    return accountRepository.getUsers();
  }

  @RequestMapping(value = "/me", method = RequestMethod.GET)
  @ResponseBody
  public Account getLoggedInUser(Principal principal) {
    if (principal != null) {
      return accountRepository.getAccount(principal);
    }
    return null;
  }
}

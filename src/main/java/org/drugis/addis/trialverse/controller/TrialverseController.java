package org.drugis.addis.trialverse.controller;

import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.trialverse.model.SemanticOutcome;
import org.drugis.addis.trialverse.model.Trialverse;
import org.drugis.addis.trialverse.repository.TrialverseRepository;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by connor on 2/12/14.
 */
@Controller
public class TrialverseController {

  final static Logger logger = LoggerFactory.getLogger(TrialverseController.class);

  @Inject
  private AccountRepository accountRepository;

  @Inject
  private TrialverseRepository trialverseRepository;

  @Inject
  private TriplestoreService triplestoreService;

  @RequestMapping(value = "/namespaces", method = RequestMethod.GET)
  @ResponseBody
  public Collection<Trialverse> query(Principal currentUser) throws MethodNotAllowedException {
    Account user = accountRepository.findAccountByUsername(currentUser.getName());
    if (user != null) {
      return trialverseRepository.query();
    } else {
      throw new MethodNotAllowedException();
    }
  }

  @RequestMapping(value = "/namespaces/{namespaceId}", method = RequestMethod.GET)
  @ResponseBody
  public Trialverse get(Principal currentUser, @PathVariable Long namespaceId) throws MethodNotAllowedException, ResourceDoesNotExistException {
    Account user = accountRepository.findAccountByUsername(currentUser.getName());
    if (user != null) {
      return trialverseRepository.get(namespaceId);
    } else {
      throw new MethodNotAllowedException();
    }
  }

  @RequestMapping(value = "/namespaces/{namespaceId}/outcomes", method = RequestMethod.GET)
  @ResponseBody
  public Collection<SemanticOutcome> queryOutcomes(Principal currentUser, @PathVariable Long namespaceId) throws MethodNotAllowedException, ResourceDoesNotExistException {
    Account user = accountRepository.findAccountByUsername(currentUser.getName());
    if (user != null) {
      return triplestoreService.getOutcomes(namespaceId);
    } else {
      throw new MethodNotAllowedException();
    }
  }

  @ResponseStatus(HttpStatus.FORBIDDEN)
  @ExceptionHandler(MethodNotAllowedException.class)
  public String handleMethodNotAllowed(HttpServletRequest request) {
    logger.error("Access to resource not authorised.\n{}", request.getRequestURL());
    return "redirect:/error/403";
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(ResourceDoesNotExistException.class)
  public String handleResourceDoesNotExist(HttpServletRequest request) {
    logger.error("Access to non-existent resource.\n{}", request.getRequestURL());
    return "redirect:/error/404";
  }
}

package org.drugis.trialverse.study;

import org.drugis.trialverse.security.Account;
import org.drugis.trialverse.security.repository.AccountRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

/**
 * Created by daan on 19-11-14.
 */
@Controller
@RequestMapping(value = "/datasets/{datasetUUID}/studies")
public class StudyController {
  @Inject
  private AccountRepository accountRepository;

  @RequestMapping(method = RequestMethod.POST)
  public void updateStudy(HttpServletRequest request, Principal currentUser, @RequestBody Object requestGraph, @PathVariable String datasetUUID) {
    Account currentUserAccount = accountRepository.findAccountByUsername(currentUser.getName());

  }
}

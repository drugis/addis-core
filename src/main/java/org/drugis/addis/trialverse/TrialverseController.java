package org.drugis.addis.trialverse;

import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.projects.Project;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by connor on 2/12/14.
 */
@Controller
public class TrialverseController {
  @Inject
  private AccountRepository accountRepository;
  @RequestMapping(value="/trialverse", method= RequestMethod.GET)

  @ResponseBody
  public Collection<Trialverse> query(Principal currentUser) throws MethodNotAllowedException {
    Account user = accountRepository.findAccountByUsername(currentUser.getName());
    if (user != null) {
      return Arrays.asList(new Trialverse("testname1"), new Trialverse("testname2"));
    } else {
      throw new MethodNotAllowedException();
    }
  }
}

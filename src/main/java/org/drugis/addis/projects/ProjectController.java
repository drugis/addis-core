package org.drugis.addis.projects;

import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.security.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.drugis.addis.security.repository.AccountRepository;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Collection;

/**
 * Created by daan on 2/6/14.
 */
@Controller
@RequestMapping(value="/projects")
public class ProjectController {

  final static Logger logger = LoggerFactory.getLogger(ProjectController.class);

  @Inject
  private ProjectRepository projectsRepository;
  @Inject
  private AccountRepository accountRepository;

  @RequestMapping(value="", method= RequestMethod.GET)
  @ResponseBody
  public Collection<Project> query(Principal currentUser) throws MethodNotAllowedException {
    Account user = accountRepository.findAccountByUsername(currentUser.getName());
    if (user != null) {
      return projectsRepository.query(user.getId());
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

}

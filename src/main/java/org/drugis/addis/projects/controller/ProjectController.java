package org.drugis.addis.projects.controller;

import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.projects.Project;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.security.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.drugis.addis.security.repository.AccountRepository;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.Charset;
import java.security.Principal;
import java.util.Collection;

/**
 * Created by daan on 2/6/14.
 */
@Controller
@RequestMapping(value="")
public class ProjectController {

  final static Logger logger = LoggerFactory.getLogger(ProjectController.class);

  public final static MediaType APPLICATION_JSON_UTF8 = new MediaType(
          MediaType.APPLICATION_JSON.getType(),
          MediaType.APPLICATION_JSON.getSubtype(),
          Charset.forName("utf8"));
  public final static String APPLICATION_JSON_UTF8_VALUE =  "application/json; charset=UTF-8";

  @Inject
  private ProjectRepository projectsRepository;
  @Inject
  private AccountRepository accountRepository;

  @RequestMapping(value="/projects", method= RequestMethod.GET)
  @ResponseBody
  public Collection<Project> query(Principal currentUser, @RequestParam(required = false) Integer owner) throws MethodNotAllowedException {
    Account user = accountRepository.findAccountByUsername(currentUser.getName());
    System.out.println("user found");
    if (user != null) {
      return owner == null ? projectsRepository.query() : projectsRepository.queryByOwnerId(owner);
    } else {
      throw new MethodNotAllowedException();
    }
  }

 @RequestMapping(value="/projects", method=RequestMethod.POST, produces = {APPLICATION_JSON_UTF8_VALUE})
	@ResponseBody
	public Project create(HttpServletRequest request, HttpServletResponse response, Principal currentUser, @RequestBody Project body) {
		Account user = accountRepository.findAccountByUsername(currentUser.getName());
		Project Project = projectsRepository.create(user, body.getName(), body.getDescription(), body.getNamespace());
		response.setStatus(HttpServletResponse.SC_CREATED);
		response.setHeader("Location", request.getRequestURL() + "/" + Project.getId());
		return Project;
	}

  @ResponseStatus(HttpStatus.FORBIDDEN)
	@ExceptionHandler(MethodNotAllowedException.class)
	public String handleMethodNotAllowed(HttpServletRequest request) {
		logger.error("Access to resource not authorised.\n{}", request.getRequestURL());
		return "redirect:/error/403";
	}
}

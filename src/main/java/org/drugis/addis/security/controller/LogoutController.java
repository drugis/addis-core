package org.drugis.addis.security.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by connor on 21-7-16.
 */
@Controller
public class LogoutController {

  final static Logger logger = LoggerFactory.getLogger(LogoutController.class);

  @RequestMapping(value="/signout", method = RequestMethod.GET)
  public String signout (HttpServletRequest request, HttpServletResponse response) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null) {
      logger.info("sign out");
      new SecurityContextLogoutHandler().logout(request, response, auth);
    }
    return "redirect:/";
  }

}

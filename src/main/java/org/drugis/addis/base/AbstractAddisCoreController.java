package org.drugis.addis.base;

import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by daan on 3/5/14.
 */
public class AbstractAddisCoreController {
  final static Logger logger = LoggerFactory.getLogger(AbstractAddisCoreController.class);

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

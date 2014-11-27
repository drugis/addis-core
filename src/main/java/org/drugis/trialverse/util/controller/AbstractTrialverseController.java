package org.drugis.trialverse.util.controller;

import org.drugis.trialverse.exception.MethodNotAllowedException;
import org.drugis.trialverse.exception.ResourceDoesNotExistException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by connor on 27-11-14.
 */
public class AbstractTrialverseController {

  final static Logger logger = LoggerFactory.getLogger(AbstractTrialverseController.class);

  public static class ErrorResponse {
    public int code;
    public String message;

    public ErrorResponse(int code, String message) {
      this.code = code;
      this.message = message;
    }
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(ResourceDoesNotExistException.class)
  @ResponseBody
  public ErrorResponse handleResourceDoesNotExist(HttpServletRequest request) {
    logger.error("Resource not found.\n{}", request.getRequestURL());
    return new ErrorResponse(404, "Not found");
  }

  @ResponseStatus(HttpStatus.FORBIDDEN)
  @ExceptionHandler(MethodNotAllowedException.class)
  @ResponseBody
  public ErrorResponse handleResourceNotOwned(HttpServletRequest request) {
    logger.error("Access to resource not authorised.\n{}", request.getRequestURL());
    return new ErrorResponse(403, "Not authorized");
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(InvalidDataAccessApiUsageException.class)
  @ResponseBody
  public ErrorResponse handleIllegalArgumentException(HttpServletRequest request) {
    logger.error("Bad request.\n{}", request.getQueryString());
    return new ErrorResponse(400, "Bad request");
  }

}

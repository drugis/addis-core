package org.drugis.trialverse.error;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ErrorController {
  @RequestMapping(value = "/error/{code}", method = RequestMethod.GET)
  public ModelAndView getErrorView(@PathVariable int code) {
    ModelAndView errorModelAndView = new ModelAndView("error/errorPage");
    errorModelAndView.addObject("errorCode", code);
    errorModelAndView.addObject("reasonPhrase", HttpStatus.valueOf(code).getReasonPhrase());
    return errorModelAndView;
  }
}

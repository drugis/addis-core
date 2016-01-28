package org.drugis.trialverse.error;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@Controller
public class ErrorController {
  @RequestMapping(value = "/error/{code}")
  public ModelAndView getErrorView(@PathVariable int code) {
    ModelAndView errorModelAndView = new ModelAndView("error/errorPage");
    errorModelAndView.addObject("errorCode", code);
    errorModelAndView.addObject("reasonPhrase", HttpStatus.valueOf(code).getReasonPhrase());
    return errorModelAndView;
  }

  @RequestMapping(value = "/error/{code}", produces = "application/json")
  @ResponseBody
  public Object getErrorJSON(@PathVariable int code) {
    Map<String, String> map = new HashMap<>();
    map.put("code", Integer.toString(code));
    map.put("message", HttpStatus.valueOf(code).getReasonPhrase());
    return map;
  }
}

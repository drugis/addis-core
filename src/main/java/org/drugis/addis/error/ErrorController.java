package org.drugis.addis.error;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ErrorController {
    @RequestMapping(value="/error/{code}")
    public ModelAndView getErrorView(@PathVariable int code) {
        ModelAndView errorModelAndView = new ModelAndView("../error/errorPage.html");
        errorModelAndView.addObject("errorCode", code);
        errorModelAndView.addObject("reasonPhrase", HttpStatus.valueOf(code).getReasonPhrase());
        return errorModelAndView;
    }
}

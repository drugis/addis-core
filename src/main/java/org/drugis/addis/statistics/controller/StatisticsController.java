package org.drugis.addis.statistics.controller;

import org.drugis.addis.statistics.command.EstimatesCommand;
import org.drugis.addis.statistics.model.Estimates;
import org.drugis.addis.statistics.service.StatisticsService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class StatisticsController {

  @Inject
  private StatisticsService statisticsService;

  @RequestMapping(value = "/statistics/estimates", method = RequestMethod.POST)
  @ResponseBody
  public Estimates getEstimates(HttpServletRequest request, HttpServletResponse response, @RequestBody EstimatesCommand command) {
    return statisticsService.getEstimates(command);
  }
}

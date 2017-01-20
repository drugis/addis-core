package org.drugis.addis.statistics.controller;

import org.drugis.addis.statistics.command.EstimatesCommand;
import org.drugis.addis.statistics.model.Estimates;
import org.drugis.addis.statistics.service.StatisticsService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;

/**
 * Created by daan on 20-1-17.
 */
@Controller
public class StatisticsController {

  @Inject
  private StatisticsService statisticsService;


  @RequestMapping(value = "/statistics/estimates", method = RequestMethod.POST)
  public Estimates getEstimates(@RequestBody EstimatesCommand command) {
    return statisticsService.getEstimates(command);
  }
}

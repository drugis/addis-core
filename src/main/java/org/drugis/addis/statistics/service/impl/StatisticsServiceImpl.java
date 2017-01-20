package org.drugis.addis.statistics.service.impl;

import org.drugis.addis.statistics.command.EstimatesCommand;
import org.drugis.addis.statistics.model.Estimates;
import org.drugis.addis.statistics.service.StatisticsService;
import org.springframework.stereotype.Service;

/**
 * Created by daan on 20-1-17.
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {
  @Override
  public Estimates getEstimates(EstimatesCommand command) {
    return null;
  }
}

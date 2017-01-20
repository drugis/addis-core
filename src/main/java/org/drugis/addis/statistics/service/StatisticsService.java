package org.drugis.addis.statistics.service;

import org.drugis.addis.statistics.command.EstimatesCommand;
import org.drugis.addis.statistics.model.Estimates;

/**
 * Created by daan on 20-1-17.
 */
public interface StatisticsService {
  Estimates getEstimates(EstimatesCommand command);
}

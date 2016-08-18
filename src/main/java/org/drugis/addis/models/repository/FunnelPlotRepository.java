package org.drugis.addis.models.repository;

import org.drugis.addis.models.FunnelPlot;
import org.drugis.addis.models.controller.command.CreateFunnelPlotCommand;

import java.util.List;

/**
 * Created by daan on 18-8-16.
 */
public interface FunnelPlotRepository {
  FunnelPlot create(CreateFunnelPlotCommand createFunnelPlotCommand);

  List<FunnelPlot> query(Integer modelId);
}

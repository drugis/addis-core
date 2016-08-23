package org.drugis.addis.models.repository.impl;

import org.drugis.addis.models.FunnelPlot;
import org.drugis.addis.models.FunnelPlotComparison;
import org.drugis.addis.models.controller.command.CreateFunnelPlotCommand;
import org.drugis.addis.models.repository.FunnelPlotRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by daan on 18-8-16.
 */
@Repository
public class FunnelPlotRepositoryImpl implements FunnelPlotRepository {

  @Qualifier("emAddisCore")
  @PersistenceContext(unitName = "addisCore")
  EntityManager em;

  @Override
  public FunnelPlot create(CreateFunnelPlotCommand createFunnelPlotCommand) {
    FunnelPlot funnelPlot = new FunnelPlot(createFunnelPlotCommand.getModelId());

    em.persist(funnelPlot);
    em.flush();

    List<FunnelPlotComparison> includedComparisons = createFunnelPlotCommand.getIncludedComparisons()
            .stream()
            .map(command -> new FunnelPlotComparison(funnelPlot.getId(), command.getT1(), command.getT2(), command.getBiasDirection()))
            .collect(Collectors.toList());

    funnelPlot.setIncludedComparisons(includedComparisons);
    return funnelPlot;
  }

  @Override
  public List<FunnelPlot> query(Integer modelId) {
    TypedQuery<FunnelPlot> query = em.createQuery("FROM FunnelPlot WHERE modelId = :modelId", FunnelPlot.class);
    query.setParameter("modelId", modelId);
    return query.getResultList();
  }
}

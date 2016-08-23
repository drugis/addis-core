package org.drugis.addis.models.repository;

import org.drugis.addis.config.JpaRepositoryTestConfig;
import org.drugis.addis.models.BiasDirection;
import org.drugis.addis.models.FunnelPlot;
import org.drugis.addis.models.FunnelPlotComparison;
import org.drugis.addis.models.controller.command.CreateFunnelPlotCommand;
import org.drugis.addis.models.controller.command.CreateFunnelPlotComparisonCommand;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by daan on 18-8-16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = {JpaRepositoryTestConfig.class})
public class FunnelPlotRepositoryTest {
  @PersistenceContext(unitName = "addisCore")
  EntityManager em;

  @Inject
  private FunnelPlotRepository funnelPlotRepository;

  @Test
  public void testCreate() {
    Integer modelId = 1;
    int t1 = 2;
    int t2 = 3;
    List<CreateFunnelPlotComparisonCommand> includedComparisons = Collections.singletonList(new CreateFunnelPlotComparisonCommand(t1, t2, BiasDirection.T_1));
    CreateFunnelPlotCommand createCommand = new CreateFunnelPlotCommand(modelId, includedComparisons);

    FunnelPlot createdPlot = funnelPlotRepository.create(createCommand);

    FunnelPlotComparison expectedComparison = new FunnelPlotComparison(createdPlot.getId(), t1, t2, BiasDirection.T_1);
    assertEquals(modelId, createdPlot.getModelId());
    assertEquals(1, createdPlot.getIncludedComparisons().size());
    assertEquals(expectedComparison, createdPlot.getIncludedComparisons().get(0));
  }

  @Test
  public void testQuery() {
    Integer modelId = 1;

    List<FunnelPlot> result = funnelPlotRepository.query(modelId);

    assertEquals(1, result.size());
    FunnelPlotComparison comparison1 = new FunnelPlotComparison(-1, 2, 3, BiasDirection.T_1);
    FunnelPlotComparison comparison2 = new FunnelPlotComparison(-1, 3, 4, BiasDirection.T_2);
    List<FunnelPlotComparison> expectedComparisons = Arrays.asList(comparison1, comparison2);
    FunnelPlot expectedFunnelPlot = new FunnelPlot(-1, 1, expectedComparisons);
    assertEquals(expectedFunnelPlot, result.get(0));
  }

}
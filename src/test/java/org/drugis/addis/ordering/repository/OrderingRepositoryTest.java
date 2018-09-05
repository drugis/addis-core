package org.drugis.addis.ordering.repository;

import org.drugis.addis.config.JpaRepositoryTestConfig;
import org.drugis.addis.ordering.Ordering;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.transaction.Transactional;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {JpaRepositoryTestConfig.class})
@Transactional
public class OrderingRepositoryTest {

  @Inject
  private OrderingRepository orderingRepository;

  private Integer workspaceId = -10;

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testGet() {
    Ordering ordering = orderingRepository.get(workspaceId);
    Ordering expectedOrdering = new Ordering(workspaceId,
            "ordering: {alternatives:[\"intervention 1\",\"intervention 2\"]," +
                    " criteria:[\"outcome 2\",\"outcome 2\"]}");
    assertEquals(ordering.getAnalysisId(),expectedOrdering.getAnalysisId());
    assertEquals(ordering.getOrdering(),expectedOrdering.getOrdering());
  }

  @Test
  public void testPut(){
    orderingRepository.put(40,"some orderings");
    Ordering result = orderingRepository.get(40);

    assertEquals(result.getAnalysisId(), new Integer(40));
    assertEquals(result.getOrdering(), "some orderings");
  }

}


package org.drugis.addis.outcomes.service;

import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.outcomes.repository.OutcomeRepository;
import org.drugis.addis.outcomes.service.impl.OutcomeServiceImpl;
import org.drugis.addis.trialverse.model.SemanticVariable;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by connor on 10-6-16.
 */
public class OutcomeServiceTest {

    @Mock
    private OutcomeRepository outcomeRepository;

    @InjectMocks
    private OutcomeService outcomeService;

    private Integer outcomeId = 2;
    private Integer projectId = 1;


    @Before
    public void setUp() {
      outcomeService = new OutcomeServiceImpl();
      initMocks(this);
    }

  @Test
  public void editNameAndMotivation() throws Exception {
    String name = "name";
    String motivation = "motivation";
    Outcome oldIntervention = new Outcome(outcomeId, projectId, "oldName", "oldMotivation", new SemanticVariable(URI.create("uri"), "uriLabel"));
    when(outcomeRepository.get(projectId, outcomeId)).thenReturn(oldIntervention);
    when(outcomeRepository.isExistingOutcomeName(outcomeId, "name")).thenReturn(false);
    Outcome abstractIntervention = outcomeService.updateNameAndMotivation(projectId, outcomeId, name, motivation);
    assertEquals(outcomeId, abstractIntervention.getId());
    assertEquals(name, abstractIntervention.getName());
    assertEquals(motivation, abstractIntervention.getMotivation());
  }

  @Test(expected = Exception.class)
  public void editNameAndMotivationCheckDuplicateName() throws Exception {
    String name = "name";
    String motivation = "motivation";
    when(outcomeRepository.isExistingOutcomeName(outcomeId, "name")).thenReturn(true);
    outcomeService.updateNameAndMotivation(projectId, outcomeId, name, motivation);
  }

  @Test(expected = Exception.class)
  public void editNameAndMotivationOtherProject() throws Exception {
    String name = "name";
    String motivation = "motivation";
    when(outcomeRepository.isExistingOutcomeName(outcomeId, "name")).thenReturn(false);
    when(outcomeRepository.get(projectId, outcomeId)).thenReturn(null);
    outcomeService.updateNameAndMotivation(projectId, outcomeId, name, motivation);
  }


}

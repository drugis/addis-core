package org.drugis.addis.outcomes;

import org.apache.commons.lang3.StringUtils;
import org.drugis.addis.trialverse.model.SemanticVariable;
import org.junit.Test;

import java.net.URI;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by daan on 3/12/14.
 */
public class OutcomeCommandTest {
  @Test
  public void testGet() throws Exception {
    OutcomeCommand outcomeCmdWithNoMotivation = new OutcomeCommand(1, "name", 1, null, new SemanticVariable(URI.create("uri"), "label"));
    assertEquals(StringUtils.EMPTY, outcomeCmdWithNoMotivation.getMotivation());

    String motivation = "motivation";
    OutcomeCommand filledCommand = new OutcomeCommand(1, "name", -1,motivation, new SemanticVariable(URI.create("uri"), "label"));
    assertEquals(motivation, filledCommand.getMotivation());
  }

  @Test(expected = Exception.class)
  public void testInvalidDirection() throws Exception {
    Integer invalidDirection = 99;
    OutcomeCommand outcomeCmdWithNoMotivation = new OutcomeCommand(1, "name", invalidDirection, null, new SemanticVariable(URI.create("uri"), "label"));
  }
}

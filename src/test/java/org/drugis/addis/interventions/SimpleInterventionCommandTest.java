package org.drugis.addis.interventions;

/**
 * Created by daan on 3/12/14.
 */

import org.apache.commons.lang3.StringUtils;
import org.drugis.addis.interventions.controller.command.AbstractInterventionCommand;
import org.drugis.addis.interventions.controller.command.SimpleInterventionCommand;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;


public class SimpleInterventionCommandTest {
  @Test
  public void testGetMotivation() {
    AbstractInterventionCommand interventionCmdWithNoMotivation = new SimpleInterventionCommand(1, "name", null, "uri", "label");
    assertEquals(StringUtils.EMPTY, interventionCmdWithNoMotivation.getMotivation());
    String motivation = "motivation";
    AbstractInterventionCommand filledCommand = new SimpleInterventionCommand(1, "name", motivation, "uri", "label");
    assertEquals(motivation, filledCommand.getMotivation());
  }
}

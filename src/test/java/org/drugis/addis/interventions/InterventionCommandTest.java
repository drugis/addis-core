/**
 * Created by daan on 3/12/14.
 */

import org.apache.commons.lang.StringUtils;
import org.drugis.addis.interventions.model.InterventionCommand;
import org.drugis.addis.trialverse.model.SemanticIntervention;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;


public class InterventionCommandTest {
  @Test
  public void testGetMotivation() {
    InterventionCommand interventionCmdWithNoMotivation = new InterventionCommand(1, "name", null, new SemanticIntervention("uri", "label"));
    assertEquals(StringUtils.EMPTY, interventionCmdWithNoMotivation.getMotivation());
    String motivation = "motivation";
    InterventionCommand filledCommand = new InterventionCommand(1, "name", motivation, new SemanticIntervention("uri", "label"));
    assertEquals(motivation, filledCommand.getMotivation());
  }
}

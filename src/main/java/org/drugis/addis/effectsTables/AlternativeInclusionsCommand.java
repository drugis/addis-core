package org.drugis.addis.effectsTables;

import java.util.List;

/**
 * Created by joris on 7-4-17.
 */
public class AlternativeInclusionsCommand {
  private List<String> alternativeIds;

  public AlternativeInclusionsCommand(){}

  public AlternativeInclusionsCommand(List<String> alternativeIds) {
    this.alternativeIds = alternativeIds;
  }

  public List<String> getAlternativeIds() {
    return alternativeIds;
  }
}

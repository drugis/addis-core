package org.drugis.addis.effectsTables;

/**
 * Created by joris on 7-4-17.
 */
public class AlternativeIdCommand {
  private String alternativeId;

  public AlternativeIdCommand(){}

  public AlternativeIdCommand(String id) {
    this.alternativeId = id;
  }

  public String getAlternativeId() {
    return alternativeId;
  }
}

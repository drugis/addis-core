package org.drugis.addis.effectsTables;

/**
 * Created by joris on 7-4-17.
 */
public class AlternativeIdCommand {
  private Integer alternativeId;

  public AlternativeIdCommand(){}

  public AlternativeIdCommand(Integer id) {
    this.alternativeId = id;
  }

  public Integer getAlternativeId() {
    return alternativeId;
  }
}

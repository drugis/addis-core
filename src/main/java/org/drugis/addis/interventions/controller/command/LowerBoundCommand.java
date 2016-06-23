package org.drugis.addis.interventions.controller.command;

import org.drugis.addis.interventions.model.LowerBoundType;

import java.net.URI;

/**
 * Created by daan on 1-4-16.
 */
public class LowerBoundCommand extends AbstractBoundCommand {
  private LowerBoundType type;

  public LowerBoundCommand() {
    super();
  }

  public LowerBoundCommand(LowerBoundType type, Double value, String unitName, String unitPeriod, URI unitConcept) {
    super(value, unitName, unitPeriod, unitConcept);
    this.type = type;
  }

  public LowerBoundType getType() {
    return type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    LowerBoundCommand that = (LowerBoundCommand) o;

    return type == that.type;

  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + type.hashCode();
    return result;
  }
}

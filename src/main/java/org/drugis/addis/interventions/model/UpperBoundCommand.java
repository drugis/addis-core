package org.drugis.addis.interventions.model;

import java.net.URI;

/**
 * Created by daan on 5-4-16.
 */
public class UpperBoundCommand extends AbstractBoundCommand {
  private UpperBoundType type;

  public UpperBoundCommand() {
    super();
  }

  public UpperBoundCommand(UpperBoundType type, Double value, String unitName, String unitPeriod, URI unitConcept) {
    super(value, unitName, unitPeriod, unitConcept);
    this.type = type;
  }

  public UpperBoundType getType() {
    return type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    UpperBoundCommand that = (UpperBoundCommand) o;

    return type == that.type;

  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + type.hashCode();
    return result;
  }
}

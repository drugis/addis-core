package org.drugis.addis.interventions;

import java.util.List;

/**
 * Created by joris on 11-4-17.
 */
public  class SetMultipliersCommand {
  private List<InterventionMultiplierCommand> multipliers;

  public SetMultipliersCommand() {
  }

  public SetMultipliersCommand(List<InterventionMultiplierCommand> multipliers) {
    this.multipliers = multipliers;
  }

  public List<InterventionMultiplierCommand> getMultipliers() {
    return multipliers;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SetMultipliersCommand that = (SetMultipliersCommand) o;

    return multipliers.equals(that.multipliers);
  }

  @Override
  public int hashCode() {
    return multipliers.hashCode();
  }
}

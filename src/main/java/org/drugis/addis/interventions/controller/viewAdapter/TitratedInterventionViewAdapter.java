package org.drugis.addis.interventions.controller.viewAdapter;

import org.drugis.addis.interventions.model.DoseConstraint;
import org.drugis.addis.interventions.model.TitratedDoseIntervention;

/**
 * Created by connor on 1-6-16.
 */
public class TitratedInterventionViewAdapter extends SingleInterventionViewAdapter {

  private DoseConstraint minConstraint;
  private DoseConstraint maxConstraint;

  public TitratedInterventionViewAdapter(TitratedDoseIntervention intervention) {
    super(intervention);
    this.minConstraint = intervention.getMinConstraint();
    this.maxConstraint = intervention.getMaxConstraint();
  }

  public DoseConstraint getMinConstraint() {
    return minConstraint;
  }

  public DoseConstraint getMaxConstraint() {
    return maxConstraint;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    TitratedInterventionViewAdapter that = (TitratedInterventionViewAdapter) o;

    if (minConstraint != null ? !minConstraint.equals(that.minConstraint) : that.minConstraint != null) return false;
    return maxConstraint != null ? maxConstraint.equals(that.maxConstraint) : that.maxConstraint == null;

  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (minConstraint != null ? minConstraint.hashCode() : 0);
    result = 31 * result + (maxConstraint != null ? maxConstraint.hashCode() : 0);
    return result;
  }
}

package org.drugis.addis.interventions.controller.viewAdapter;

import org.drugis.addis.interventions.model.DoseConstraint;
import org.drugis.addis.interventions.model.FixedDoseIntervention;

/**
 * Created by connor on 1-6-16.
 */
public class FixedInterventionViewAdapter extends SingleInterventionViewAdapter {

  private DoseConstraint constraint;

  public FixedInterventionViewAdapter(FixedDoseIntervention intervention) {
    super(intervention);
    this.constraint = intervention.getConstraint();
  }

  public DoseConstraint getConstraint() {
    return constraint;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    FixedInterventionViewAdapter that = (FixedInterventionViewAdapter) o;

    return constraint != null ? constraint.equals(that.constraint) : that.constraint == null;

  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (constraint != null ? constraint.hashCode() : 0);
    return result;
  }
}

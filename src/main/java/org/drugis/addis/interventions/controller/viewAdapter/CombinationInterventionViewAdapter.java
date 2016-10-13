package org.drugis.addis.interventions.controller.viewAdapter;

import org.drugis.addis.interventions.model.CombinationIntervention;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by connor on 1-6-16.
 */
public class CombinationInterventionViewAdapter extends AbstractInterventionViewAdapter {

  private Set<Integer> interventionIds = new HashSet<>();

  public CombinationInterventionViewAdapter(CombinationIntervention intervention) {
    super(intervention);
    interventionIds = new HashSet<>(intervention.getInterventionIds());
  }

  public Set<Integer> getInterventionIds() {
    return interventionIds;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    CombinationInterventionViewAdapter that = (CombinationInterventionViewAdapter) o;

    return interventionIds.equals(that.interventionIds);

  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + interventionIds.hashCode();
    return result;
  }
}

package org.drugis.addis.interventions.controller.viewAdapter;

import org.drugis.addis.interventions.model.InterventionSet;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Daan on 06/10/2016.
 */
public class InterventionSetViewAdapter extends AbstractInterventionViewAdapter {

  private Set<Integer> interventionIds = new HashSet<>();

  public InterventionSetViewAdapter(InterventionSet interventionSet) {
    super(interventionSet);
    interventionIds = interventionSet.getInterventionIds();
  }

  public Set<Integer> getInterventionIds() {
    return interventionIds;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    InterventionSetViewAdapter that = (InterventionSetViewAdapter) o;

    return interventionIds.equals(that.interventionIds);

  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + interventionIds.hashCode();
    return result;
  }
}

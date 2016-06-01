package org.drugis.addis.interventions.controller.viewAdapter;

import org.drugis.addis.interventions.model.CombinationIntervention;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by connor on 1-6-16.
 */
public class CombinationInterventionViewAdapter extends AbstractInterventionViewAdapter{

  private Set<Integer> singleInterventionIds = new HashSet<>();

  public CombinationInterventionViewAdapter(CombinationIntervention intervention){
    super(intervention);
    singleInterventionIds = new HashSet<>(intervention.getSingleInterventionIds());
  }

  public Set<Integer> getSingleInterventionIds() {
    return singleInterventionIds;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    CombinationInterventionViewAdapter that = (CombinationInterventionViewAdapter) o;

    return singleInterventionIds.equals(that.singleInterventionIds);

  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + singleInterventionIds.hashCode();
    return result;
  }
}

package org.drugis.addis.interventions.model;

import org.drugis.addis.interventions.controller.viewAdapter.AbstractInterventionViewAdapter;
import org.drugis.addis.interventions.controller.viewAdapter.CombinationInterventionViewAdapter;

import javax.persistence.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by connor on 31-5-16.
 */
@Entity
@PrimaryKeyJoinColumn(name = "combinationInterventionId", referencedColumnName = "id")
public class CombinationIntervention extends MultipleIntervention {
  public CombinationIntervention() {
    super();
  }

  public CombinationIntervention(Integer id, Integer project, String name, String motivation, Set<Integer> interventionIds) {
    super(id, project, name, motivation);
    this.interventionIds = interventionIds;
  }
  public CombinationIntervention(Integer project, String name, String motivation, Set<Integer> interventionIds) {
    this(null, project, name, motivation, interventionIds);
  }
  public Set<Integer> getInterventionIds() {
    return Collections.unmodifiableSet(interventionIds);
  }

  @Override
  public AbstractInterventionViewAdapter toViewAdapter() {
    return new CombinationInterventionViewAdapter(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    CombinationIntervention that = (CombinationIntervention) o;

    return interventionIds.equals(that.interventionIds);

  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + interventionIds.hashCode();
    return result;
  }
}

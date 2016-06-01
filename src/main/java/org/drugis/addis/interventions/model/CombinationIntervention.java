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
public class CombinationIntervention extends AbstractIntervention {

  @ElementCollection
  @CollectionTable(
          name="InterventionCombination",
          joinColumns=@JoinColumn(name="combinationInterventionId")
  )
  @Column(name="singleInterventionId")
  private Set<Integer> singleInterventionIds = new HashSet<>();

  public CombinationIntervention() {
    super();
  }

  public CombinationIntervention(Integer id, Integer project, String name, String motivation, Set<Integer> singleInterventionIds) {
    super(id, project, name, motivation);
    this.singleInterventionIds = singleInterventionIds;
  }

  public Set<Integer> getSingleInterventionIds() {
    return Collections.unmodifiableSet(singleInterventionIds);
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

    return singleInterventionIds.equals(that.singleInterventionIds);

  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + singleInterventionIds.hashCode();
    return result;
  }
}

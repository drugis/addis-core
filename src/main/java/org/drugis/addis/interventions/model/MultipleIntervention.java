package org.drugis.addis.interventions.model;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.JoinColumn;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by joris on 15-12-16.
 */
public abstract class MultipleIntervention extends AbstractIntervention {
  @ElementCollection
  @CollectionTable(
          name="multipleInterventionItem",
          joinColumns=@JoinColumn(name="multipleInterventionId")
  )
  @Column(name="interventionId")
  private Set<Integer> interventionIds = new HashSet<>();

  public Set<Integer> getInterventionIds() {
    return interventionIds;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    MultipleIntervention that = (MultipleIntervention) o;

    return interventionIds.equals(that.interventionIds);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + interventionIds.hashCode();
    return result;
  }
}

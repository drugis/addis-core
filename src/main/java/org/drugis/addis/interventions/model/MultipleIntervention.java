package org.drugis.addis.interventions.model;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.JoinColumn;
import java.util.Collections;
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

  public MultipleIntervention() {
  }

  public MultipleIntervention(Integer id, Integer project, String name, String motivation, Set<Integer> interventionIds) {
    super(id, project, name, motivation);
    this.interventionIds = interventionIds;
  }

  public Set<Integer> getInterventionIds() {
    return Collections.unmodifiableSet(interventionIds);
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

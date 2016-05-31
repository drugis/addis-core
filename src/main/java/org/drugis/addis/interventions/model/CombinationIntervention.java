package org.drugis.addis.interventions.model;

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

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinTable(name = "CombinationIntervention_Intervention",
          joinColumns = {@JoinColumn(name = "combinationInterventionId", referencedColumnName = "combinationInterventionId")},
          inverseJoinColumns = {@JoinColumn(name = "interventionId", referencedColumnName = "id")})
  private Set<AbstractIntervention> interventions = new HashSet<>();

  public CombinationIntervention() {
    super();
  }

  public CombinationIntervention(Integer id, Integer project, String name, String motivation, Set<AbstractIntervention> interventions) {
    super(id, project, name, motivation);
    this.interventions = interventions;
  }

  public Set<AbstractIntervention> getInterventions() {
    return Collections.unmodifiableSet(interventions);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    CombinationIntervention that = (CombinationIntervention) o;

    return interventions.equals(that.interventions);

  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + interventions.hashCode();
    return result;
  }
}

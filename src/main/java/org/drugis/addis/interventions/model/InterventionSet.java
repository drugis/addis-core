package org.drugis.addis.interventions.model;

import org.drugis.addis.interventions.controller.viewAdapter.AbstractInterventionViewAdapter;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.JoinColumn;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by daan on 11-10-16.
 */
public class InterventionSet extends AbstractIntervention {

  @ElementCollection
  @CollectionTable(
          name="InterventionCombination",
          joinColumns=@JoinColumn(name="combinationInterventionId")
  )
  @Column(name="interventionId")
  private Set<Integer> interventionIds  = new HashSet<>();

  public InterventionSet(Integer id, Integer projectId, String name, String motivation, Set<Integer> interventionIds) {
    super(id, projectId, name, motivation);

    this.interventionIds = interventionIds;
  }

  @Override
  public AbstractInterventionViewAdapter toViewAdapter() {
    return null;
  }
}

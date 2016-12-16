package org.drugis.addis.interventions.model;

import org.drugis.addis.interventions.controller.viewAdapter.AbstractInterventionViewAdapter;
import org.drugis.addis.interventions.controller.viewAdapter.InterventionSetViewAdapter;

import javax.persistence.*;
import java.util.Collections;
import java.util.Set;

/**
 * Created by Daan on 06/10/2016.
 */
@Entity
@PrimaryKeyJoinColumn(name = "interventionSetId", referencedColumnName = "id")
public class InterventionSet extends MultipleIntervention {
  public InterventionSet() {
    super();
  }

  public InterventionSet(Integer id, Integer project, String name, String motivation, Set<Integer> interventionIds) {
    super(id, project, name, motivation, interventionIds);
  }

  public InterventionSet(Integer id, String name, String motivation, Set<Integer> interventionIds) {
    this(null, id, name, motivation, interventionIds);
  }

  @Override
  public AbstractInterventionViewAdapter toViewAdapter() {
    return new InterventionSetViewAdapter(this);
  }
}

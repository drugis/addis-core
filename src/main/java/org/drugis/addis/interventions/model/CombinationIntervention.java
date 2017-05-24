package org.drugis.addis.interventions.model;

import org.drugis.addis.interventions.controller.command.SetMultipliersCommand;
import org.drugis.addis.interventions.controller.viewAdapter.AbstractInterventionViewAdapter;
import org.drugis.addis.interventions.controller.viewAdapter.CombinationInterventionViewAdapter;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.persistence.*;
import java.util.Set;

/**
 * Created by connor on 31-5-16.
 */
@Entity
@PrimaryKeyJoinColumn(name = "combinationInterventionId", referencedColumnName = "multipleInterventionId")
public class CombinationIntervention extends MultipleIntervention {
  public CombinationIntervention() {
    super();
  }

  public CombinationIntervention(Integer id, Integer project, String name, String motivation, Set<Integer> interventionIds) {
    super(id, project, name, motivation, interventionIds);
  }

  public CombinationIntervention(Integer project, String name, String motivation, Set<Integer> interventionIds) {
    this(null, project, name, motivation, interventionIds);
  }

  @Override
  public AbstractInterventionViewAdapter toViewAdapter() {
    return new CombinationInterventionViewAdapter(this);
  }

  @Override
  public void updateMultipliers(SetMultipliersCommand command) {
    throw new NotImplementedException(); // should always update component interventions directly instead
  }
}

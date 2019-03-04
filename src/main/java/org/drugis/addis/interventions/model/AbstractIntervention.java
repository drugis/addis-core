package org.drugis.addis.interventions.model;

import org.drugis.addis.interventions.controller.command.SetMultipliersCommand;
import org.drugis.addis.interventions.controller.viewAdapter.AbstractInterventionViewAdapter;

import javax.persistence.*;
import java.net.URI;
import java.util.Objects;

/**
 * Created by daan on 5-4-16.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class AbstractIntervention {
  @Id
  @SequenceGenerator(name = "intervention_sequence", sequenceName = "shared_intervention_id_seq", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "intervention_sequence")
  private Integer id;
  private Integer project;
  private String name;
  private String motivation;

  public AbstractIntervention() {
  }

  public AbstractIntervention(Integer id, Integer project, String name, String motivation) {
    this.id = id;
    this.project = project;
    this.name = name;
    this.motivation = motivation;
  }

  public AbstractIntervention(Integer project, String name, String motivation) {
    this(null, project, name, motivation);
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setMotivation(String motivation) {
    this.motivation = motivation;
  }

  public Integer getId() {
    return id;
  }

  public Integer getProject() {
    return project;
  }

  public String getName() {
    return name;
  }

  public String getMotivation() {
    return motivation;
  }

  public abstract AbstractInterventionViewAdapter toViewAdapter();

  public abstract void updateMultipliers(SetMultipliersCommand command);

  static void updateConstraint(DoseConstraint constraint, Double multiplier, URI unitConcept, String unitName) {
    if (constraint.getLowerBound() != null && constraint.getLowerBound().isMatched(unitConcept, unitName)) {
      constraint.getLowerBound().setConversionMultiplier(multiplier);
    }
    if (constraint.getUpperBound() != null && constraint.getUpperBound().isMatched(unitConcept, unitName)) {
      constraint.getUpperBound().setConversionMultiplier(multiplier);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AbstractIntervention that = (AbstractIntervention) o;
    return Objects.equals(id, that.id) &&
            Objects.equals(project, that.project) &&
            Objects.equals(name, that.name) &&
            Objects.equals(motivation, that.motivation);
  }

  @Override
  public int hashCode() {

    return Objects.hash(id, project, name, motivation);
  }
}

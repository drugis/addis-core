package org.drugis.addis.interventions.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.persistence.*;

/**
 * Created by daan on 5-4-16.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = SimpleIntervention.class, name = "simple"),
        @JsonSubTypes.Type(value = FixedDoseIntervention.class, name = "fixed"),
        @JsonSubTypes.Type(value = TitratedDoseIntervention.class, name = "titrated"),
        @JsonSubTypes.Type(value = BothDoseTypesIntervention.class, name = "both")})
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AbstractIntervention that = (AbstractIntervention) o;

    if (id != null ? !id.equals(that.id) : that.id != null) return false;
    if (!project.equals(that.project)) return false;
    if (!name.equals(that.name)) return false;
    return motivation != null ? motivation.equals(that.motivation) : that.motivation == null;

  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + project.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + (motivation != null ? motivation.hashCode() : 0);
    return result;
  }
}

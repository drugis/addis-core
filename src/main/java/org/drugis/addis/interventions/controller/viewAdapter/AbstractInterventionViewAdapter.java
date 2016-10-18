package org.drugis.addis.interventions.controller.viewAdapter;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.drugis.addis.interventions.model.AbstractIntervention;

/**
 * Created by connor on 1-6-16.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = SimpleInterventionViewAdapter.class, name = "simple"),
        @JsonSubTypes.Type(value = FixedInterventionViewAdapter.class, name = "fixed"),
        @JsonSubTypes.Type(value = TitratedInterventionViewAdapter.class, name = "titrated"),
        @JsonSubTypes.Type(value = BothDoseTypesInterventionViewAdapter.class, name = "both"),
        @JsonSubTypes.Type(value = CombinationInterventionViewAdapter.class, name = "combination"),
        @JsonSubTypes.Type(value = InterventionSetViewAdapter.class, name="class")})
public abstract class AbstractInterventionViewAdapter {

  private Integer id;
  private Integer project;
  private String name;
  private String motivation;

  protected AbstractInterventionViewAdapter(AbstractIntervention intervention){
    this.id = intervention.getId();
    this.project = intervention.getProject();
    this.name = intervention.getName();
    this.motivation = intervention.getMotivation();
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

    AbstractInterventionViewAdapter that = (AbstractInterventionViewAdapter) o;

    if (!id.equals(that.id)) return false;
    if (!project.equals(that.project)) return false;
    if (!name.equals(that.name)) return false;
    return motivation != null ? motivation.equals(that.motivation) : that.motivation == null;

  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + project.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + (motivation != null ? motivation.hashCode() : 0);
    return result;
  }
}

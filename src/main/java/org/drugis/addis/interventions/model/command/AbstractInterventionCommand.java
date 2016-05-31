package org.drugis.addis.interventions.model.command;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.interventions.model.InvalidConstraintException;

/**
 * Created by connor on 3/6/14.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "doseType")
@JsonSubTypes({@JsonSubTypes.Type(value = SimpleInterventionCommand.class, name = "simple"),
        @JsonSubTypes.Type(value = FixedInterventionCommand.class, name = "fixed"),
        @JsonSubTypes.Type(value = TitratedInterventionCommand.class, name = "titrated"),
        @JsonSubTypes.Type(value = BothDoseTypesInterventionCommand.class, name = "both"),
        @JsonSubTypes.Type(value = CombinationInterventionCommand.class, name = "combination")})
public abstract class AbstractInterventionCommand {
  private Integer projectId;
  private String name;
  private String motivation;
  private String semanticInterventionLabel;
  private String semanticInterventionUuid;

  public AbstractInterventionCommand() {
  }

  public AbstractInterventionCommand(Integer projectId, String name, String motivation, String semanticInterventionLabel, String semanticInterventionUuid) {
    this.projectId = projectId;
    this.name = name;
    this.motivation = motivation;
    this.semanticInterventionLabel = semanticInterventionLabel;
    this.semanticInterventionUuid = semanticInterventionUuid;
    if (motivation == null) {
      this.motivation = "";
    }
  }

  public abstract AbstractIntervention toIntervention() throws InvalidConstraintException;

  public Integer getProjectId() {
    return projectId;
  }

  public String getName() {
    return name;
  }

  public String getMotivation() {
    return motivation;
  }

  public String getSemanticInterventionLabel() {
    return semanticInterventionLabel;
  }

  public String getSemanticInterventionUuid() {
    return semanticInterventionUuid;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AbstractInterventionCommand that = (AbstractInterventionCommand) o;

    if (!projectId.equals(that.projectId)) return false;
    if (!name.equals(that.name)) return false;
    if (motivation != null ? !motivation.equals(that.motivation) : that.motivation != null) return false;
    if (!semanticInterventionLabel.equals(that.semanticInterventionLabel)) return false;
    return semanticInterventionUuid.equals(that.semanticInterventionUuid);

  }

  @Override
  public int hashCode() {
    int result = projectId.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + (motivation != null ? motivation.hashCode() : 0);
    result = 31 * result + semanticInterventionLabel.hashCode();
    result = 31 * result + semanticInterventionUuid.hashCode();
    return result;
  }
}

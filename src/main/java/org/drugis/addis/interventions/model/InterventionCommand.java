package org.drugis.addis.interventions.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang.StringUtils;
import org.drugis.addis.trialverse.model.SemanticIntervention;

/**
 * Created by connor on 3/6/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class InterventionCommand {
  private Integer projectId;
  private String name;
  private String motivation;
  private SemanticIntervention semanticIntervention;
  private ConstraintCommand fixedDoseConstraint;
  private ConstraintCommand titratedDoseMinConstraint;
  private ConstraintCommand titratedDoseMaxConstraint;
  private ConstraintCommand bothDoseTypesMinConstraint;
  private ConstraintCommand bothDoseTypesMaxConstraint;

  public InterventionCommand() {
  }

  public InterventionCommand(Integer projectId, String name, String motivation, SemanticIntervention semanticIntervention) {
    this.projectId = projectId;
    this.name = name;
    this.motivation = motivation;
    this.semanticIntervention = semanticIntervention;
  }

  public InterventionCommand(Integer projectId, String name, String motivation, SemanticIntervention semanticIntervention,
                             ConstraintCommand fixedDoseConstraintCommand, ConstraintCommand titratedDoseMinConstraint,
                             ConstraintCommand titratedDoseMaxConstraint, ConstraintCommand bothDoseTypesMinConstraint,
                             ConstraintCommand bothDoseTypesMaxConstraint) {
    this.projectId = projectId;
    this.name = name;
    this.motivation = motivation;
    this.semanticIntervention = semanticIntervention;
    this.fixedDoseConstraint = fixedDoseConstraintCommand;
    this.titratedDoseMinConstraint = titratedDoseMinConstraint;
    this.titratedDoseMaxConstraint = titratedDoseMaxConstraint;
    this.bothDoseTypesMinConstraint = bothDoseTypesMinConstraint;
    this.bothDoseTypesMaxConstraint = bothDoseTypesMaxConstraint;
  }

  public Integer getProjectId() {
    return projectId;
  }

  public String getName() {
    return name;
  }

  public String getMotivation() {
    return motivation == null ? StringUtils.EMPTY : motivation;
  }

  public SemanticIntervention getSemanticIntervention() {
    return semanticIntervention;
  }

  public ConstraintCommand getFixedDoseConstraint() {
    return fixedDoseConstraint;
  }

  public ConstraintCommand getTitratedDoseMinConstraint() {
    return titratedDoseMinConstraint;
  }

  public ConstraintCommand getTitratedDoseMaxConstraint() {
    return titratedDoseMaxConstraint;
  }

  public ConstraintCommand getBothDoseTypesMinConstraint() {
    return bothDoseTypesMinConstraint;
  }

  public ConstraintCommand getBothDoseTypesMaxConstraint() {
    return bothDoseTypesMaxConstraint;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    InterventionCommand that = (InterventionCommand) o;

    if (!projectId.equals(that.projectId)) return false;
    if (!name.equals(that.name)) return false;
    if (motivation != null ? !motivation.equals(that.motivation) : that.motivation != null) return false;
    if (!semanticIntervention.equals(that.semanticIntervention)) return false;
    if (fixedDoseConstraint != null ? !fixedDoseConstraint.equals(that.fixedDoseConstraint) : that.fixedDoseConstraint != null)
      return false;
    if (titratedDoseMinConstraint != null ? !titratedDoseMinConstraint.equals(that.titratedDoseMinConstraint) : that.titratedDoseMinConstraint != null)
      return false;
    if (titratedDoseMaxConstraint != null ? !titratedDoseMaxConstraint.equals(that.titratedDoseMaxConstraint) : that.titratedDoseMaxConstraint != null)
      return false;
    if (bothDoseTypesMinConstraint != null ? !bothDoseTypesMinConstraint.equals(that.bothDoseTypesMinConstraint) : that.bothDoseTypesMinConstraint != null)
      return false;
    return bothDoseTypesMaxConstraint != null ? bothDoseTypesMaxConstraint.equals(that.bothDoseTypesMaxConstraint) : that.bothDoseTypesMaxConstraint == null;

  }

  @Override
  public int hashCode() {
    int result = projectId.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + (motivation != null ? motivation.hashCode() : 0);
    result = 31 * result + semanticIntervention.hashCode();
    result = 31 * result + (fixedDoseConstraint != null ? fixedDoseConstraint.hashCode() : 0);
    result = 31 * result + (titratedDoseMinConstraint != null ? titratedDoseMinConstraint.hashCode() : 0);
    result = 31 * result + (titratedDoseMaxConstraint != null ? titratedDoseMaxConstraint.hashCode() : 0);
    result = 31 * result + (bothDoseTypesMinConstraint != null ? bothDoseTypesMinConstraint.hashCode() : 0);
    result = 31 * result + (bothDoseTypesMaxConstraint != null ? bothDoseTypesMaxConstraint.hashCode() : 0);
    return result;
  }
}

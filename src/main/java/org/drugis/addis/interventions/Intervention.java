package org.drugis.addis.interventions;

import org.drugis.addis.trialverse.model.SemanticIntervention;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by daan on 3/6/14.
 */
@Entity
public class Intervention implements Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;
  private Integer project;
  private String name;
  private String motivation;
  private String semanticInterventionLabel;
  private String semanticInterventionUri;

  public Intervention() {
  }

  public Intervention(Integer project, String name, String motivation, SemanticIntervention semanticIntervention) {
    this(null, project, name, motivation, semanticIntervention);
  }

  public Intervention(Integer id, Integer project, String name, String motivation, SemanticIntervention semanticIntervention) {
    this.id = id;
    this.project = project;
    this.name = name;
    this.motivation = motivation;
    this.semanticInterventionLabel = semanticIntervention.getLabel();
    this.semanticInterventionUri = semanticIntervention.getUri();
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

  public String getSemanticInterventionLabel() {
    return semanticInterventionLabel;
  }

  public String getSemanticInterventionUri() {
    return semanticInterventionUri;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Intervention that = (Intervention) o;

    if (id != null ? !id.equals(that.id) : that.id != null) return false;
    if (!motivation.equals(that.motivation)) return false;
    if (!name.equals(that.name)) return false;
    if (!project.equals(that.project)) return false;
    if (!semanticInterventionLabel.equals(that.semanticInterventionLabel)) return false;
    if (!semanticInterventionUri.equals(that.semanticInterventionUri)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + project.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + motivation.hashCode();
    result = 31 * result + semanticInterventionLabel.hashCode();
    result = 31 * result + semanticInterventionUri.hashCode();
    return result;
  }
}

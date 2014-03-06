package org.drugis.addis.interventions;

import org.drugis.addis.trialverse.model.SemanticIntervention;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Created by daan on 3/6/14.
 */
@Entity
public class Intervention {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;
  private String name;
  private String motivation;
  private String semanticInterventionLabel;
  private String semanticInterventionUrl;

  public Intervention() {
  }

  public Intervention(Integer id, String name, String motivation, String semanticInterventionLabel, String semanticInterventionUrl) {
    this.id = id;
    this.name = name;
    this.motivation = motivation;
    this.semanticInterventionLabel = semanticInterventionLabel;
    this.semanticInterventionUrl = semanticInterventionUrl;
  }

  public Intervention(Integer id, String name, String motivation, SemanticIntervention semanticIntervention) {
    this(id, name, motivation, semanticIntervention.getLabel(), semanticIntervention.getUri());
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getMotivation() {
    return motivation;
  }

  public void setMotivation(String motivation) {
    this.motivation = motivation;
  }

  public String getSemanticInterventionLabel() {
    return semanticInterventionLabel;
  }

  public void setSemanticInterventionLabel(String semanticInterventionLabel) {
    this.semanticInterventionLabel = semanticInterventionLabel;
  }

  public String getSemanticInterventionUrl() {
    return semanticInterventionUrl;
  }

  public void setSemanticInterventionUrl(String semanticInterventionUrl) {
    this.semanticInterventionUrl = semanticInterventionUrl;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Intervention that = (Intervention) o;

    if (!id.equals(that.id)) return false;
    if (!motivation.equals(that.motivation)) return false;
    if (!name.equals(that.name)) return false;
    if (!semanticInterventionLabel.equals(that.semanticInterventionLabel)) return false;
    if (!semanticInterventionUrl.equals(that.semanticInterventionUrl)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + motivation.hashCode();
    result = 31 * result + semanticInterventionLabel.hashCode();
    result = 31 * result + semanticInterventionUrl.hashCode();
    return result;
  }
}

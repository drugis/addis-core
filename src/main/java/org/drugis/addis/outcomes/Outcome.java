package org.drugis.addis.outcomes;

import org.drugis.addis.trialverse.model.SemanticVariable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.net.URI;

/**
 * Created by daan on 2/20/14.
 */
@Entity
public class Outcome implements Serializable{

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;
  private Integer project;
  private String name;
  private String motivation;
  private String semanticOutcomeLabel;
  private String semanticOutcomeUri;

  public Outcome() {
  }

  public Outcome(Integer id, Integer project, String name, String motivation, SemanticVariable semanticOutcome) {
    this.id = id;
    this.project = project;
    this.name = name;
    this.motivation = motivation;
    this.semanticOutcomeLabel = semanticOutcome.getLabel();
    this.semanticOutcomeUri = semanticOutcome.getUri().toString();
  }

  public Outcome(Integer project, String name, String motivation, SemanticVariable semanticOutcome) {
    this(null, project, name, motivation, semanticOutcome);
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

  public String getSemanticOutcomeLabel() {
    return semanticOutcomeLabel;
  }

  public URI getSemanticOutcomeUri() {
    return URI.create(semanticOutcomeUri);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Outcome)) return false;

    Outcome outcome = (Outcome) o;

    if (id != null ? !id.equals(outcome.id) : outcome.id != null) return false;
    if (!motivation.equals(outcome.motivation)) return false;
    if (!name.equals(outcome.name)) return false;
    if (!project.equals(outcome.project)) return false;
    if (!semanticOutcomeLabel.equals(outcome.semanticOutcomeLabel)) return false;
    if (!semanticOutcomeUri.equals(outcome.semanticOutcomeUri)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + project.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + motivation.hashCode();
    result = 31 * result + semanticOutcomeLabel.hashCode();
    result = 31 * result + semanticOutcomeUri.hashCode();
    return result;
  }
}

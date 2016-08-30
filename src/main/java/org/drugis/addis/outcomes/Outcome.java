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

  private final static int HIGHER_IS_BETTER = 1;
  private final static int LOWER_IS_BETTER = -1;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;
  private Integer project;
  private String name;
  private Integer direction;
  private String motivation;
  private String semanticOutcomeLabel;
  private String semanticOutcomeUri;

  public Outcome() {
  }

  public Outcome(Integer id, Integer project, String name, Integer direction, String motivation, SemanticVariable semanticOutcome) throws Exception {
    this.id = id;
    this.project = project;
    this.name = name;
    if(direction.intValue() != HIGHER_IS_BETTER && direction.intValue() != LOWER_IS_BETTER) {
      throw new Exception("invalid direction value, must be 1 either or -1");
    }
    this.direction = direction;
    this.motivation = motivation;
    this.semanticOutcomeLabel = semanticOutcome.getLabel();
    this.semanticOutcomeUri = semanticOutcome.getUri().toString();
  }

  public Outcome(Integer id, Integer project, String name, String motivation, SemanticVariable semanticOutcome) throws Exception {
    this(id, project, name, HIGHER_IS_BETTER, motivation, semanticOutcome);
  }

  public Outcome(Integer project, String name, Integer direction, String motivation, SemanticVariable semanticOutcome) throws Exception {
    this(null, project, name, direction, motivation, semanticOutcome);
  }

  public Outcome(Integer project, String name, String motivation, SemanticVariable semanticOutcome) throws Exception {
    this(null, project, name, HIGHER_IS_BETTER, motivation, semanticOutcome);
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

  public Integer getDirection() {
    return direction;
  }

  public String getMotivation() {
    return motivation;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setMotivation(String motivation) {
    this.motivation = motivation;
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
    if (o == null || getClass() != o.getClass()) return false;

    Outcome outcome = (Outcome) o;

    if (!id.equals(outcome.id)) return false;
    if (!project.equals(outcome.project)) return false;
    if (!name.equals(outcome.name)) return false;
    if (!direction.equals(outcome.direction)) return false;
    if (motivation != null ? !motivation.equals(outcome.motivation) : outcome.motivation != null) return false;
    if (semanticOutcomeLabel != null ? !semanticOutcomeLabel.equals(outcome.semanticOutcomeLabel) : outcome.semanticOutcomeLabel != null)
      return false;
    return semanticOutcomeUri != null ? semanticOutcomeUri.equals(outcome.semanticOutcomeUri) : outcome.semanticOutcomeUri == null;

  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + project.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + direction.hashCode();
    result = 31 * result + (motivation != null ? motivation.hashCode() : 0);
    result = 31 * result + (semanticOutcomeLabel != null ? semanticOutcomeLabel.hashCode() : 0);
    result = 31 * result + (semanticOutcomeUri != null ? semanticOutcomeUri.hashCode() : 0);
    return result;
  }
}

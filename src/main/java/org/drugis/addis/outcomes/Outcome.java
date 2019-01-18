package org.drugis.addis.outcomes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.drugis.addis.trialverse.model.SemanticVariable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.net.URI;
import java.util.Objects;

/**
 * Created by daan on 2/20/14.
 */
@Entity
public class Outcome implements Serializable {

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
    if (direction != HIGHER_IS_BETTER && direction != LOWER_IS_BETTER) {
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

  public void setDirection(Integer direction) throws Exception {
    if (direction.intValue() != HIGHER_IS_BETTER && direction.intValue() != LOWER_IS_BETTER) {
      throw new Exception("invalid direction value, must be 1 either or -1");
    }
    this.direction = direction;
  }

  public String getSemanticOutcomeLabel() {
    return semanticOutcomeLabel;
  }

  public URI getSemanticOutcomeUri() {
    return URI.create(semanticOutcomeUri);
  }

  @JsonIgnore
  public SemanticVariable getSemanticVariable() {
    return new SemanticVariable(URI.create(this.semanticOutcomeUri), this.semanticOutcomeLabel);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Outcome outcome = (Outcome) o;
    return Objects.equals(id, outcome.id) &&
            Objects.equals(project, outcome.project) &&
            Objects.equals(name, outcome.name) &&
            Objects.equals(direction, outcome.direction) &&
            Objects.equals(motivation, outcome.motivation) &&
            Objects.equals(semanticOutcomeLabel, outcome.semanticOutcomeLabel) &&
            Objects.equals(semanticOutcomeUri, outcome.semanticOutcomeUri);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, project, name, direction, motivation, semanticOutcomeLabel, semanticOutcomeUri);
  }
}

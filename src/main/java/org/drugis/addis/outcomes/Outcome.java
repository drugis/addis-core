package org.drugis.addis.outcomes;

import org.drugis.addis.trialverse.model.SemanticOutcome;

import javax.persistence.*;

/**
 * Created by daan on 2/20/14.
 */
@Entity
public class Outcome {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;
  private String name;
  private String motivation;
  private String semanticOutcomeLabel;
  private String semanticOutcomeUrl;

  public Outcome() {
  }

  public Outcome(String name, String motivation, SemanticOutcome semanticOutcome) {
    this(null, name, motivation, semanticOutcome);
  }

  public Outcome(Integer id, String name, String motivation, SemanticOutcome semanticOutcome) {
    this.id = id;
    this.name = name;
    this.motivation = motivation;
    this.semanticOutcomeLabel = semanticOutcome.getLabel();
    this.semanticOutcomeUrl = semanticOutcome.getUri();
  }

  public Integer getId() {
    return id;
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

  public String getSemanticOutcomeUrl() {
    return semanticOutcomeUrl;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Outcome)) return false;

    Outcome outcome = (Outcome) o;

    if (id != null ? !id.equals(outcome.id) : outcome.id != null) return false;
    if (!motivation.equals(outcome.motivation)) return false;
    if (!name.equals(outcome.name)) return false;
    if (!semanticOutcomeLabel.equals(outcome.semanticOutcomeLabel)) return false;
    if (!semanticOutcomeUrl.equals(outcome.semanticOutcomeUrl)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + name.hashCode();
    result = 31 * result + motivation.hashCode();
    result = 31 * result + semanticOutcomeLabel.hashCode();
    result = 31 * result + semanticOutcomeUrl.hashCode();
    return result;
  }
}

package org.drugis.addis.projects;

import java.net.URI;

/**
 * Created by daan on 2/20/14.
 */
public class Outcome {
  private String name;
  private String motivation;
  private URI semanticOutcome;

  public Outcome() {
  }

  public Outcome(String name, String motivation, URI semanticOutcome) {
    this.name = name;
    this.motivation = motivation;
    this.semanticOutcome = semanticOutcome;
  }

  public String getName() {
    return name;
  }

  public String getMotivation() {
    return motivation;
  }

  public URI getSemanticOutcome() {
    return semanticOutcome;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Outcome outcome = (Outcome) o;

    if (!motivation.equals(outcome.motivation)) return false;
    if (!name.equals(outcome.name)) return false;
    if (!semanticOutcome.equals(outcome.semanticOutcome)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + motivation.hashCode();
    result = 31 * result + semanticOutcome.hashCode();
    return result;
  }
}

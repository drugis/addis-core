package org.drugis.addis.projects;

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
  private String semanticOutcome;

  public Outcome() {
  }

  public Outcome(String name, String motivation, String semanticOutcome) {
    this.name = name;
    this.motivation = motivation;
    this.semanticOutcome = semanticOutcome;
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

  public String getSemanticOutcome() {
    return semanticOutcome;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Outcome outcome = (Outcome) o;

    if (!id.equals(outcome.id)) return false;
    if (!motivation.equals(outcome.motivation)) return false;
    if (!name.equals(outcome.name)) return false;
    if (!semanticOutcome.equals(outcome.semanticOutcome)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + motivation.hashCode();
    result = 31 * result + semanticOutcome.hashCode();
    return result;
  }
}

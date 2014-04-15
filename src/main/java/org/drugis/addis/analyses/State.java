package org.drugis.addis.analyses;

/**
 * Created by daan on 4-4-14.
 */
public class State {
  private String problem;

  public State(String problem) {
    this.problem = problem;
  }

  @Override
  public String toString() {
    return "{ \"problem\": " + problem + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    State state = (State) o;

    if (problem != null ? !problem.equals(state.problem) : state.problem != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return problem != null ? problem.hashCode() : 0;
  }
}

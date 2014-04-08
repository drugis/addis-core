package org.drugis.addis.scenarios;

import org.drugis.addis.analyses.State;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Created by connor on 3-4-14.
 */
@Entity
public class Scenario {

  public final static String DEFAULT_TITLE = "Default";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  // refers to analysis but named workspace due to mcda-web
  private Integer workspace;
  private String title;
  private String state;

  public Scenario() {
  }

  public Scenario(Integer workspace, String title, State state) {
    this(null, workspace, title, state);
  }

  public Scenario(Integer id, Integer workspace, String title, State state) {
    this.id = id;
    this.workspace = workspace;
    this.title = title;
    this.state = state.toString();
  }

  public Integer getId() {
    return id;
  }

  public Integer getWorkspace() {
    return workspace;
  }

  public String getTitle() {
    return title;
  }

  public String getState() {
    return state;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Scenario)) return false;

    Scenario scenario = (Scenario) o;

    if (id != null ? !id.equals(scenario.id) : scenario.id != null) return false;
    if (!state.equals(scenario.state)) return false;
    if (!title.equals(scenario.title)) return false;
    if (!workspace.equals(scenario.workspace)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + workspace.hashCode();
    result = 31 * result + title.hashCode();
    result = 31 * result + state.hashCode();
    return result;
  }
}

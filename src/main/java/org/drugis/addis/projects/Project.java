package org.drugis.addis.projects;

import org.drugis.addis.security.Account;
import org.drugis.addis.trialverse.Trialverse;

/**
 * Created by daan on 2/6/14.
 */
public class Project {
  private Account owner;
  private int id;
  private String name;
  private String description;
  private Trialverse trialverse;

  public Project() {
  }

  public Project(int id, Account owner, String name, String description, Trialverse trialverse) {
    this.id = id;
    this.owner = owner;
    this.name = name;
    this.description = description;
    this.trialverse = trialverse;
  }

  public int getId() {
    return id;
  }

  public Account getOwner() {
    return owner;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public Trialverse getTrialverse() {
    return trialverse;
  }

  public void setTrialverse(Trialverse trialverse) {
    this.trialverse = trialverse;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Project project = (Project) o;

    if (id != project.id) return false;
    if (description != null ? !description.equals(project.description) : project.description != null) return false;
    if (!name.equals(project.name)) return false;
    if (!owner.equals(project.owner)) return false;
    if (!trialverse.equals(project.trialverse)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = owner.hashCode();
    result = 31 * result + id;
    result = 31 * result + name.hashCode();
    result = 31 * result + (description != null ? description.hashCode() : 0);
    result = 31 * result + trialverse.hashCode();
    return result;
  }
}

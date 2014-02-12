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
}

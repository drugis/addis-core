package org.drugis.addis.projects;

import org.drugis.addis.security.Account;

/**
 * Created by daan on 2/6/14.
 */
public class Project {
  private Account owner;
  private int id;
  private String name;
  private String description;

  public Project() {
  }

  public Project(int id, Account owner, String name, String description) {
    this.id = id;
    this.owner = owner;
    this.name = name;
    this.description = description;
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
}

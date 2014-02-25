package org.drugis.addis.projects;

import org.drugis.addis.security.Account;
import org.drugis.addis.trialverse.Trialverse;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by daan on 2/6/14.
 */
@Entity
public class Project {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(targetEntity = Account.class)
  @JoinColumn(name = "owner")
  private Account owner;

  @Column
  private String name;

  @Column
  private String description;

  @Embedded
  private Trialverse trialverse;

  @OneToMany(cascade = CascadeType.ALL)
  @JoinColumn(name="project")
  private List<Outcome> outcomes = new ArrayList<>();

  public Project() {
  }

  public Project(Integer id, Account owner, String name, String description, Trialverse trialverse) {
    this.id = id;
    this.owner = owner;
    this.name = name;
    this.description = description;
    this.trialverse = trialverse;
  }

  public Project(Account owner, String name, String description, Trialverse trialverse) {
    this.owner = owner;
    this.name = name;
    this.description = description;
    this.trialverse = trialverse;
  }

  public Project(Account owner, String name, String description) {
    this.owner = owner;
    this.name = name;
    this.description = description;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Account getOwner() {
    return owner;
  }

  public void setOwner(Account owner) {
    this.owner = owner;
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

  public void setDescription(String description) {
    this.description = description;
  }

  public Trialverse getTrialverse() {
    return trialverse;
  }

  public void setTrialverse(Trialverse trialverse) {
    this.trialverse = trialverse;
  }

  public void addOutcome(Outcome outcome) {
    outcomes.add(outcome);
  }

  public void removeOutcome(Outcome outcome) {
    outcomes.remove(outcome);
  }

  public List<Outcome> getOutcomes() {
    return outcomes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Project project = (Project) o;

    if (!description.equals(project.description)) return false;
    if (!id.equals(project.id)) return false;
    if (!name.equals(project.name)) return false;
    if (!outcomes.equals(project.outcomes)) return false;
    if (!owner.equals(project.owner)) return false;
    if (!trialverse.equals(project.trialverse)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + owner.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + description.hashCode();
    result = 31 * result + trialverse.hashCode();
    result = 31 * result + outcomes.hashCode();
    return result;
  }
}

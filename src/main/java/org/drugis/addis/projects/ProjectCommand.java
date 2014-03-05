package org.drugis.addis.projects;

import org.drugis.addis.outcomes.OutcomeCommand;
import org.drugis.addis.security.Account;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daan on 3/5/14.
 */
public class ProjectCommand {
  private Account account;
  private String name;
  private String description;
  private Integer trialverseId;
  private List<OutcomeCommand> outcomes = new ArrayList<>();

  public ProjectCommand() {
  }

  public ProjectCommand(Account account, String name, String description, Integer trialverseId, List<OutcomeCommand> outcomes) {
    this.account = account;
    this.name = name;
    this.description = description;
    this.trialverseId = trialverseId;
    this.outcomes = outcomes;
  }

  public Account getAccount() {
    return account;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public Integer getTrialverseId() {
    return trialverseId;
  }

  public List<OutcomeCommand> getOutcomes() {
    return outcomes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ProjectCommand that = (ProjectCommand) o;

    if (!account.equals(that.account)) return false;
    if (!description.equals(that.description)) return false;
    if (!name.equals(that.name)) return false;
    if (!outcomes.equals(that.outcomes)) return false;
    if (!trialverseId.equals(that.trialverseId)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = account.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + description.hashCode();
    result = 31 * result + trialverseId.hashCode();
    result = 31 * result + outcomes.hashCode();
    return result;
  }
}

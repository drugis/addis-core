package org.drugis.addis.trialverse.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daan on 3/19/14.
 */
public class Study {
  private String uid;
  private String name;
  private String title;
  private List<String> outcomeUids = new ArrayList<>();
  private List<String> interventionUids = new ArrayList<>();

  public Study() {
  }

  public Study(String uid, String name, String title, List<String> outcomeUids, List<String> interventionUids) {
    this.uid = uid;
    this.name = name;
    this.title = title;
    this.outcomeUids = outcomeUids;
    this.interventionUids = interventionUids;
  }

  public String getUid() {
    return uid;
  }

  public String getName() {
    return name;
  }

  public String getTitle() {
    return title;
  }

  public List<String> getOutcomeUids() {
    return outcomeUids;
  }

  public List<String> getInterventionUids() {
    return interventionUids;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Study study = (Study) o;

    if (!interventionUids.equals(study.interventionUids)) return false;
    if (!name.equals(study.name)) return false;
    if (!outcomeUids.equals(study.outcomeUids)) return false;
    if (!title.equals(study.title)) return false;
    if (!uid.equals(study.uid)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = uid.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + title.hashCode();
    result = 31 * result + outcomeUids.hashCode();
    result = 31 * result + interventionUids.hashCode();
    return result;
  }
}

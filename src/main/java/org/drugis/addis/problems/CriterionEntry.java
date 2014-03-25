package org.drugis.addis.problems;

/**
 * Created by connor on 25-3-14.
 */
public class CriterionEntry {
  private String title;

  public CriterionEntry() {
  }

  public CriterionEntry(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CriterionEntry)) return false;

    CriterionEntry that = (CriterionEntry) o;

    if (!title.equals(that.title)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return title.hashCode();
  }
}

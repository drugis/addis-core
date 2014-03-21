package org.drugis.addis.problems;

/**
 * Created by daan on 3/21/14.
 */
public class AlternativeEntry {
  private String title;

  public AlternativeEntry(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AlternativeEntry that = (AlternativeEntry) o;

    if (!title.equals(that.title)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return title.hashCode();
  }
}

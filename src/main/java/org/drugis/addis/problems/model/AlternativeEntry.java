package org.drugis.addis.problems.model;

/**
 * Created by daan on 3/21/14.
 */
public class AlternativeEntry {
  private Integer alternative;
  private String title;

  public AlternativeEntry(Integer alternative, String title) {
    this.alternative = alternative;
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

  public Integer getAlternative() {
    return alternative;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AlternativeEntry that = (AlternativeEntry) o;

    if (!alternative.equals(that.alternative)) return false;
    if (!title.equals(that.title)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = alternative.hashCode();
    result = 31 * result + title.hashCode();
    return result;
  }
}

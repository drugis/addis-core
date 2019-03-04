package org.drugis.addis.problems.model;

import java.util.Objects;

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
    return Objects.equals(alternative, that.alternative) &&
            Objects.equals(title, that.title);
  }

  @Override
  public int hashCode() {

    return Objects.hash(alternative, title);
  }
}

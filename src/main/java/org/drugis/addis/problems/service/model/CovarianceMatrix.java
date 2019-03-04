package org.drugis.addis.problems.service.model;

import java.util.List;
import java.util.Objects;

/**
 * Created by joris on 14-6-17.
 */
public class CovarianceMatrix {
  private List<String> rownames;
  private List<String> colnames;
  private List<List<Double>> data;

  public CovarianceMatrix(List<String> rownames, List<String> colnames, List<List<Double>> data) {
    this.rownames = rownames;
    this.colnames = colnames;
    this.data = data;
  }

  public List<String> getRownames() {
    return rownames;
  }

  public List<String> getColnames() {
    return colnames;
  }

  public List<List<Double>> getData() {
    return data;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CovarianceMatrix that = (CovarianceMatrix) o;
    return Objects.equals(rownames, that.rownames) &&
        Objects.equals(colnames, that.colnames) &&
        Objects.equals(data, that.data);
  }

  @Override
  public int hashCode() {

    return Objects.hash(rownames, colnames, data);
  }
}

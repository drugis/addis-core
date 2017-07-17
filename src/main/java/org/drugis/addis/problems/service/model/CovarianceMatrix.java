package org.drugis.addis.problems.service.model;

import java.util.List;

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

    if (!rownames.equals(that.rownames)) return false;
    if (!colnames.equals(that.colnames)) return false;
    return data.equals(that.data);
  }

  @Override
  public int hashCode() {
    int result = rownames.hashCode();
    result = 31 * result + colnames.hashCode();
    result = 31 * result + data.hashCode();
    return result;
  }
}

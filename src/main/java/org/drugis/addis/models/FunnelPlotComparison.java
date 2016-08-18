package org.drugis.addis.models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.io.Serializable;

/**
 * Created by daan on 18-8-16.
 */
@Entity
@IdClass(FunnelPlotComparison.FunnelPlotComparisonPK.class)
public class FunnelPlotComparison {
  @Id
  private Integer plotId;
  @Id
  private Integer t1;
  @Id
  private Integer t2;

  private BiasDirection biasDirection;

  public FunnelPlotComparison() {
  }

  public FunnelPlotComparison(Integer plotId, Integer t1, Integer t2, BiasDirection biasDirection) {
    this.plotId = plotId;
    this.t1 = t1;
    this.t2 = t2;
    this.biasDirection = biasDirection;
  }

  public Integer getPlotId() {
    return plotId;
  }

  public Integer getT1() {
    return t1;
  }

  public Integer getT2() {
    return t2;
  }

  public BiasDirection getBiasDirection() {
    return biasDirection;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    FunnelPlotComparison that = (FunnelPlotComparison) o;

    if (!plotId.equals(that.plotId)) return false;
    if (!t1.equals(that.t1)) return false;
    if (!t2.equals(that.t2)) return false;
    return biasDirection == that.biasDirection;

  }

  @Override
  public int hashCode() {
    int result = plotId.hashCode();
    result = 31 * result + t1.hashCode();
    result = 31 * result + t2.hashCode();
    result = 31 * result + biasDirection.hashCode();
    return result;
  }

  static class FunnelPlotComparisonPK implements Serializable {
    protected Integer plotId;
    protected Integer t1;
    protected Integer t2;

    public FunnelPlotComparisonPK() {
    }

    public FunnelPlotComparisonPK(Integer plotId, Integer t1, Integer t2) {
      this.plotId = plotId;
      this.t1 = t1;
      this.t2 = t2;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      FunnelPlotComparisonPK that = (FunnelPlotComparisonPK) o;

      if (!plotId.equals(that.plotId)) return false;
      if (!t1.equals(that.t1)) return false;
      return t2.equals(that.t2);

    }

    @Override
    public int hashCode() {
      int result = plotId.hashCode();
      result = 31 * result + t1.hashCode();
      result = 31 * result + t2.hashCode();
      return result;
    }
  }
}

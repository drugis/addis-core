package org.drugis.addis.models;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by daan on 18-8-16.
 */
@Entity
public class FunnelPlot {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;
  private Integer modelId;
  @OneToMany(fetch = FetchType.EAGER, mappedBy = "plotId", cascade = CascadeType.ALL)
  private List<FunnelPlotComparison> includedComparisons = new ArrayList<>();

  public FunnelPlot() {}

  public FunnelPlot(Integer modelId) {
    this.modelId = modelId;
  }

  public FunnelPlot(Integer id, Integer modelId, List<FunnelPlotComparison> includedComparisons) {
    this.id = id;
    this.modelId = modelId;
    this.includedComparisons = includedComparisons;
  }

  public Integer getId() {
    return id;
  }

  public Integer getModelId() {
    return modelId;
  }

  public List<FunnelPlotComparison> getIncludedComparisons() {
    return Collections.unmodifiableList(includedComparisons);
  }

  public void setIncludedComparisons(List<FunnelPlotComparison> includedComparisons) {
    this.includedComparisons = includedComparisons;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    FunnelPlot that = (FunnelPlot) o;

    if (!id.equals(that.id)) return false;
    if (!modelId.equals(that.modelId)) return false;
    return includedComparisons.equals(that.includedComparisons);

  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + modelId.hashCode();
    result = 31 * result + includedComparisons.hashCode();
    return result;
  }

}

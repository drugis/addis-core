package org.drugis.addis.problems.model;

import org.drugis.addis.problems.model.problemEntry.*;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

public class RelativeEffectData {
  private Map<URI, RelativeDataEntry> data;
  private String scale;

  public RelativeEffectData() {
  }

  public RelativeEffectData(Map<URI, RelativeDataEntry> data) {
    this.data =  data;
    this.scale = findScale(data);
  }

  private String findScale(Map<URI, RelativeDataEntry> data) {
    if (data == null || data.isEmpty()) {
      return "no relative data";
    }
    AbstractProblemEntry entry = data.get(data.keySet().iterator().next()).getOtherArms().get(0);
    if (entry instanceof ContrastDichotomousOddsProblemEntry) {
      return "log odds ratio";
    }
    if (entry instanceof ContrastDichotomousRiskProblemEntry) {
      return "log risk ratio";
    }
    if (entry instanceof ContrastMDProblemEntry) {
      return "mean difference";
    }
    if (entry instanceof ContrastSMDProblemEntry) {
      return "standardized mean difference";
    }
    if (entry instanceof ContrastSurvivalHazardProblemEntry) {
      return "log hazard ratio";
    }
    return "no scale set";
  }


  public Map<URI, RelativeDataEntry> getData() {
    return data;
  }

  public String getScale() {
    return scale;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RelativeEffectData that = (RelativeEffectData) o;
    return Objects.equals(data, that.data) &&
            Objects.equals(scale, that.scale);
  }

  @Override
  public int hashCode() {

    return Objects.hash(data, scale);
  }
}

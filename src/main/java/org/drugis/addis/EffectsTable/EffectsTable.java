package org.drugis.addis.EffectsTable;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by joris on 4-4-17.
 */
@Entity
public class EffectsTable {
  @Id
  private Integer analysisId;
  Map<Integer, Boolean> table;

  public EffectsTable() {
  }
  public EffectsTable(Integer analysisId) {
    this.analysisId = analysisId;
    this.table = new HashMap<>();
  }
  public void setInclusion(Integer alternative, Boolean truthValue){
    table.put(alternative, truthValue);
  }
}

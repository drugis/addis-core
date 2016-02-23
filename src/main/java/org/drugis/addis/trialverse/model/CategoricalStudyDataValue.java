package org.drugis.addis.trialverse.model;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by connor on 2-10-14.
 */
public class CategoricalStudyDataValue extends AbstractStudyDataValue {

  private List<Pair<String, Integer>> values = new ArrayList<>();

  public CategoricalStudyDataValue(String armInstanceUid, String label, Boolean isArm) {
    super(armInstanceUid, label, isArm);
  }

  public List<Pair<String, Integer>> getValues() {
    return values;
  }
}

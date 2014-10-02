package org.drugis.addis.trialverse.model;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by connor on 2-10-14.
 */
public class CategoricalStudyDataArmValue extends AbstractStudyDataArmValue {

  private List<Pair<String, Integer>> values = new ArrayList<>();

  public CategoricalStudyDataArmValue(String armInstanceUid, String armLabel) {
    super(armInstanceUid, armLabel);
  }

  public List<Pair<String, Integer>> getValues() {
    return values;
  }
}

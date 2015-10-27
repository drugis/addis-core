package org.drugis.addis.trialverse.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by connor on 21-8-14.
 */
public class StudyDataMoment {

  private final String relativeToAnchorOntology;
  private final String timeOffsetDuration;
  private final String relativeToEpochLabel;

  private List<AbstractStudyDataArmValue> studyDataArmValues = new ArrayList<>();

  public StudyDataMoment(String relativeToAnchorOntology, String timeOffsetDuration, String relativeToEpochLabel) {
    this.relativeToAnchorOntology = relativeToAnchorOntology;
    this.timeOffsetDuration = timeOffsetDuration;
    this.relativeToEpochLabel = relativeToEpochLabel;
  }

  public String getRelativeToAnchorOntology() {
    return relativeToAnchorOntology;
  }

  public String getTimeOffsetDuration() {
    return timeOffsetDuration;
  }

  public String getRelativeToEpochLabel() {
    return relativeToEpochLabel;
  }

  public List<AbstractStudyDataArmValue> getStudyDataArmValues() {
    return studyDataArmValues;
  }
}

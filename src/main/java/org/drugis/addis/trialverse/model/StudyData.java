package org.drugis.addis.trialverse.model;

import org.drugis.addis.trialverse.model.emun.StudyDataSection;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by connor on 21-8-14.
 */
public class StudyData {
  private StudyDataSection studyDataSection;
  private List<AbstractStudyDataArmValue> studyDataArmValues = new ArrayList<>();
  private String studyDataTypeUri;
  private String studyDataTypeLabel;

  private String relativeToAnchorOntology;
  private String timeOffsetDuration;
  private String relativeToEpochLabel;


  private StudyData(StudyDataBuilder builder) {
    this.studyDataSection = builder.studyDataSection;
    this.studyDataTypeUri = builder.studyDataTypeUri;
    this.studyDataTypeLabel = builder.studyDataTypeLabel;
    this.relativeToAnchorOntology = builder.relativeToAnchorOntology;
    this.timeOffsetDuration = builder.timeOffsetDuration;
    this.relativeToEpochLabel = builder.relativeToEpochLabel;
  }

  public StudyDataSection getStudyDataSection() {
    return studyDataSection;
  }

  public List<AbstractStudyDataArmValue> getStudyDataArmValues() {
    return studyDataArmValues;
  }

  public String getStudyDataTypeUri() {
    return studyDataTypeUri;
  }

  public String getStudyDataTypeLabel() {
    return studyDataTypeLabel;
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

  public static class StudyDataBuilder {

    private final StudyDataSection studyDataSection;
    private final String studyDataTypeUri;
    private final String studyDataTypeLabel;

    private String relativeToAnchorOntology;
    private String timeOffsetDuration;
    private String relativeToEpochLabel;

    public StudyDataBuilder(StudyDataSection studyDataSection, String studyDataTypeUri, String studyDataTypeLabel) {
      this.studyDataSection = studyDataSection;
      this.studyDataTypeUri = studyDataTypeUri;
      this.studyDataTypeLabel = studyDataTypeLabel;
    }

    public StudyData build() {
      return new StudyData(this);
    }

    public StudyDataBuilder relativeToAnchorOntology(String relativeToAnchorOntology) {
      this.relativeToAnchorOntology = relativeToAnchorOntology;
      return this;
    }

    public StudyDataBuilder timeOffsetDuration(String timeOffsetDuration) {
      this.timeOffsetDuration = timeOffsetDuration;
      return this;
    }

    public StudyDataBuilder relativeToEpochLabel(String relativeToEpochLabel) {
      this.relativeToEpochLabel = relativeToEpochLabel;
      return this;
    }
  }
}

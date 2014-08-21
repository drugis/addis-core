package org.drugis.addis.trialverse.model;

import org.drugis.addis.trialverse.model.emun.StudyDataSection;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by connor on 21-8-14.
 */
public class StudyData {

  private final StudyDataSection studyDataSection;
  private final String studyDataTypeUri;
  private final String studyDataTypeLabel;

  private final List<StudyDataMoment> studyDataMoments = new ArrayList<>();

  public StudyData(StudyDataSection studyDataSection, String studyDataTypeUri, String studyDataTypeLabel) {
    this.studyDataSection = studyDataSection;
    this.studyDataTypeUri = studyDataTypeUri;
    this.studyDataTypeLabel = studyDataTypeLabel;
  }

  public StudyDataSection getStudyDataSection() {
    return studyDataSection;
  }

  public String getStudyDataTypeUri() {
    return studyDataTypeUri;
  }

  public String getStudyDataTypeLabel() {
    return studyDataTypeLabel;
  }

  public List<StudyDataMoment> getStudyDataMoments() {
    return studyDataMoments;
  }
}


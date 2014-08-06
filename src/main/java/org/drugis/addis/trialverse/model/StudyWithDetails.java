package org.drugis.addis.trialverse.model;

import org.joda.time.DateTime;

/**
 * Created by connor on 1-8-14.
 */
public class StudyWithDetails {

  private Study study;
  private String allocation;
  private String blinding;
  private Integer numberOfStudyCenters;
  private String objectives;  //TODO: list of strings?
  private String inclusionCriteria;//TODO: list of strings?
  private String exclusionCriteria; // TODO: not in triplestore
  private String dataSource;
  private String pubmedUrls;
  private Integer studySize;
  private String indication;
  private String investigationalDrugNames;
  private String dosing;
  private DateTime startDate;
  private DateTime endDate;
  private DateTime creationDate; //todo: or extractiondate? not in triplestore either way
  private String status;

  public StudyWithDetails() {
  }

  private StudyWithDetails(StudyWithDetailsBuilder builder) {
    this.study = builder.study;
    this.allocation = builder.allocation;
    this.blinding = builder.blinding;
    this.numberOfStudyCenters = builder.numberOfStudyCenters;
    this.objectives = builder.objectives;
    this.inclusionCriteria = builder.inclusionCriteria;
    this.exclusionCriteria = builder.exclusionCriteria;
    this.dataSource = builder.dataSource;
    this.pubmedUrls = builder.pubmedUrls;
    this.studySize = builder.studySize;
    this.indication = builder.indication;
    this.investigationalDrugNames = builder.investigationalDrugNames;
    this.dosing = builder.dosing;
    this.startDate = builder.startDate;
    this.endDate = builder.endDate;
    this.creationDate = builder.creationDate;
    this.status = builder.status;
  }

  public static class StudyWithDetailsBuilder {
    private Study study;
    private String allocation;
    private String blinding;
    private Integer numberOfStudyCenters;
    private String objectives;
    private String inclusionCriteria;
    private String exclusionCriteria;
    private String dataSource;
    private String pubmedUrls;
    private Integer studySize;
    private String indication;
    private String investigationalDrugNames;
    private String dosing;
    private DateTime startDate;
    private DateTime endDate;
    private DateTime creationDate;
    private String status;

    public StudyWithDetails build() {
      return new StudyWithDetails(this);
    }


    public StudyWithDetailsBuilder study(Study study) {
      this.study = study;
      return this;
    }

    public StudyWithDetailsBuilder allocation(String allocation) {
      this.allocation = allocation;
      return this;
    }

    public StudyWithDetailsBuilder blinding(String blinding) {
      this.blinding = blinding;
      return this;
    }

    public StudyWithDetailsBuilder numberOfStudyCenters(Integer numberOfStudyCenters) {
      this.numberOfStudyCenters = numberOfStudyCenters;
      return this;
    }

    public StudyWithDetailsBuilder objectives(String objectives) {
      this.objectives = objectives;
      return this;
    }

    public StudyWithDetailsBuilder inclusionCriteria(String inclusionCriteria) {
      this.inclusionCriteria = inclusionCriteria;
      return this;
    }

    public StudyWithDetailsBuilder exclusionCriteria(String exclusionCriteria) {
      this.exclusionCriteria = exclusionCriteria;
      return this;
    }

    public StudyWithDetailsBuilder dataSource(String dataSource) {
      this.dataSource = dataSource;
      return this;
    }

    public StudyWithDetailsBuilder pubmedUrls(String pubmedUrls) {
      this.pubmedUrls = pubmedUrls;
      return this;
    }

    public StudyWithDetailsBuilder studySize(Integer studySize) {
      this.studySize = studySize;
      return this;
    }

    public StudyWithDetailsBuilder indication(String indication) {
      this.indication = indication;
      return this;
    }

    public StudyWithDetailsBuilder investigationalDrugNames(String investigationalDrugNames) {
      this.investigationalDrugNames = investigationalDrugNames;
      return this;
    }

    public StudyWithDetailsBuilder dosing(String dosing) {
      this.dosing = dosing;
      return this;
    }

    public StudyWithDetailsBuilder startDate(DateTime startDate) {
      this.startDate = startDate;
      return this;
    }

    public StudyWithDetailsBuilder endDate(DateTime endDate) {
      this.endDate = endDate;
      return this;
    }

    public StudyWithDetailsBuilder creationDate(DateTime creationDate) {
      this.creationDate = creationDate;
      return this;
    }

    public StudyWithDetailsBuilder status(String status) {
      this.status = status;
      return this;
    }

  }

  public Study getStudy() {
    return study;
  }

  public String getAllocation() {
    return allocation;
  }

  public String getBlinding() {
    return blinding;
  }

  public Integer getNumberOfStudyCenters() {
    return numberOfStudyCenters;
  }

  public String getObjectives() {
    return objectives;
  }

  public String getInclusionCriteria() {
    return inclusionCriteria;
  }

  public String getExclusionCriteria() {
    return exclusionCriteria;
  }

  public String getDataSource() {
    return dataSource;
  }

  public String getpubmedUrls() {
    return pubmedUrls;
  }

  public Integer getStudySize() {
    return studySize;
  }

  public String getIndication() {
    return indication;
  }

  public String getInvestigationalDrugNames() {
    return investigationalDrugNames;
  }

  public String getDosing() {
    return dosing;
  }

  public DateTime getStartDate() {
    return startDate;
  }

  public DateTime getEndDate() {
    return endDate;
  }

  public String getStatus() {
    return status;
  }

  public DateTime getCreationDate() {
    return creationDate;
  }
}

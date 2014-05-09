package org.drugis.addis.trialverse.model;

/**
 * Created by connor on 9-5-14.
 */
public class TrialDataArm {
  private Long id;
  private String name;
  private Long studyId;

  public TrialDataArm() {
  }

  public TrialDataArm(Long id, Long studyId, String name) {
    this.id = id;
    this.studyId = studyId;
    this.name = name;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Long getStudyId() {
    return studyId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TrialDataArm)) return false;

    TrialDataArm that = (TrialDataArm) o;

    if (!id.equals(that.id)) return false;
    if (!name.equals(that.name)) return false;
    if (!studyId.equals(that.studyId)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + studyId.hashCode();
    return result;
  }
}

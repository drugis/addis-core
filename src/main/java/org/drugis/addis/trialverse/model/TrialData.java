package org.drugis.addis.trialverse.model;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by connor on 8-5-14.
 */
public class TrialData {

  private List<TrialDataStudy> studies = new ArrayList<>();

  public TrialData(List<Study> studies) {
    for (Study study : studies) {
      TrialDataStudy trialDataStudy = new TrialDataStudy(study.getId(), study.getName());
      this.studies.add(trialDataStudy);
    }
  }

  public TrialData(Map<Study, List<Pair<Long, String>>> studiesWithInterventions) {

    for (Study study : studiesWithInterventions.keySet()) {

      TrialDataStudy trialDataStudy = new TrialDataStudy(study.getId(), study.getName());
      List<Pair<Long, String>> drugsIdAndSemanticUris = studiesWithInterventions.get(study);
      List<TrialDataIntervention> interventions = new ArrayList<>(drugsIdAndSemanticUris.size());

      for (Pair<Long, String> drugAndSemanticUri : drugsIdAndSemanticUris) {
        interventions.add(new TrialDataIntervention(drugAndSemanticUri.getLeft(), drugAndSemanticUri.getRight()));
      }

      trialDataStudy.setTrialDataInterventions(interventions);
      this.studies.add(trialDataStudy);
    }
  }

  public List<TrialDataStudy> getStudies() {
    return studies;
  }

  public class TrialDataStudy {
    private Long studyId;
    private String title;
    private List<TrialDataIntervention> trialDataInterventions = new ArrayList<>();

    public TrialDataStudy(Long studyId, String title) {
      this.studyId = studyId;
      this.title = title;
    }

    public TrialDataStudy(Long studyId, String title, List<TrialDataIntervention> trialDataInterventions) {
      this.studyId = studyId;
      this.title = title;
      this.trialDataInterventions = trialDataInterventions;
    }

    public Long getStudyId() {
      return studyId;
    }

    public String getTitle() {
      return title;
    }

    public List<TrialDataIntervention> getTrialDataInterventions() {
      return trialDataInterventions;
    }

    public void setTrialDataInterventions(List<TrialDataIntervention> trialDataInterventions) {
      this.trialDataInterventions = trialDataInterventions;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof TrialDataStudy)) return false;

      TrialDataStudy that = (TrialDataStudy) o;

      if (!title.equals(that.title)) return false;
      if (!studyId.equals(that.studyId)) return false;
      if (trialDataInterventions != null ? !trialDataInterventions.equals(that.trialDataInterventions) : that.trialDataInterventions != null)
        return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = studyId.hashCode();
      result = 31 * result + title.hashCode();
      result = 31 * result + (trialDataInterventions != null ? trialDataInterventions.hashCode() : 0);
      return result;
    }
  }

  public class TrialDataIntervention {
    private Long drugId;
    private String uri;

    public TrialDataIntervention(Long drugId, String uri) {
      this.drugId = drugId;
      this.uri = uri;
    }

    public Long getDrugId() {
      return drugId;
    }

    public String getUri() {
      return uri;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof TrialDataIntervention)) return false;

      TrialDataIntervention that = (TrialDataIntervention) o;

      if (!drugId.equals(that.drugId)) return false;
      if (!uri.equals(that.uri)) return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = drugId.hashCode();
      result = 31 * result + uri.hashCode();
      return result;
    }
  }

  public abstract class TrialDataMeasurement {
    private Long sampleSize;

    public Long getSampleSize() {
      return sampleSize;
    }
  }

  public class TrialDataRateMeasurement extends TrialDataMeasurement {
    private Long rate;

    public Long getRate() {
      return rate;
    }
  }

  public class TrialDataContinuousMeasurement extends TrialDataMeasurement {
    Double mean;
    Double sigma;

    public Double getSigma() {
      return sigma;
    }

    public Double getMean() {
      return mean;
    }
  }

}


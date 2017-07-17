package org.drugis.addis.analyses.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.net.URI;

/**
 * Created by daan on 10-11-16.
 */
@Entity
public class MeasurementMomentInclusion {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @JsonIgnore
  private Integer id;
  private Integer analysisId;
  private URI study;
  private URI measurementMoment;

  public MeasurementMomentInclusion() {
  }

  public MeasurementMomentInclusion(Integer analysisId, URI study, URI measurementMoment) {
    this.analysisId = analysisId;
    this.study = study;
    this.measurementMoment = measurementMoment;
  }

  public Integer getId() {
    return id;
  }

  public Integer getAnalysisId() {
    return analysisId;
  }

  public URI getStudy() {
    return study;
  }

  public URI getMeasurementMoment() {
    return measurementMoment;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    MeasurementMomentInclusion that = (MeasurementMomentInclusion) o;

    if (id != null ? !id.equals(that.id) : that.id != null) return false;
    if (!analysisId.equals(that.analysisId)) return false;
    if (!study.equals(that.study)) return false;
    return measurementMoment.equals(that.measurementMoment);

  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + analysisId.hashCode();
    result = 31 * result + study.hashCode();
    result = 31 * result + measurementMoment.hashCode();
    return result;
  }
}

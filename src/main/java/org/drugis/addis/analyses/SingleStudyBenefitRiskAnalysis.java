package org.drugis.addis.analyses;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.util.ObjectToStringDeserializer;
import org.drugis.trialverse.util.Utils;

import javax.persistence.*;
import java.net.URI;
import java.util.*;

/**
 * Created by connor on 3/11/14.
 */
@Entity
@PrimaryKeyJoinColumn(name = "id", referencedColumnName = "id")
public class SingleStudyBenefitRiskAnalysis extends AbstractAnalysis {

  @JsonRawValue
  private String problem;

  private String studyGraphUri;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinTable(name = "singleStudyBenefitRiskAnalysis_Outcome",
          joinColumns = {@JoinColumn(name = "analysisId", referencedColumnName = "id")},
          inverseJoinColumns = {@JoinColumn(name = "outcomeId", referencedColumnName = "id")})
  private Set<Outcome> selectedOutcomes = new HashSet<>();

  public SingleStudyBenefitRiskAnalysis() {
  }

  public SingleStudyBenefitRiskAnalysis(Integer id, Integer projectId, String title, List<Outcome> selectedOutcomes, List<InterventionInclusion> interventionInclusions, String problem) {
    this.id = id;
    this.projectId = projectId;
    this.title = title;
    this.selectedOutcomes = selectedOutcomes == null ? this.selectedOutcomes : new HashSet<>(selectedOutcomes);
    this.interventionInclusions = interventionInclusions == null ? this.interventionInclusions : new HashSet<>(interventionInclusions);
    this.problem = problem;
  }

  public SingleStudyBenefitRiskAnalysis(Integer id, Integer projectId, String title, List<Outcome> selectedOutcomes, List<InterventionInclusion> interventionInclusions) {
    this(id, projectId, title, selectedOutcomes, interventionInclusions, null);
  }

  public SingleStudyBenefitRiskAnalysis(Integer projectId, String title, List<Outcome> selectedOutcomes, List<InterventionInclusion> interventionInclusions) {
    this(null, projectId, title, selectedOutcomes, interventionInclusions, null);
  }

  public SingleStudyBenefitRiskAnalysis(Integer projectId, String title, List<Outcome> selectedOutcomes, List<InterventionInclusion> interventionInclusions, String problem) {
    this(null, projectId, title, selectedOutcomes, interventionInclusions, problem);
  }

  @Override
  public Integer getId() {
    return id;
  }

  @Override
  public Integer getProjectId() {
    return projectId;
  }

  public String getTitle() {
    return title;
  }

  public URI getStudyGraphUri() {
    return studyGraphUri == null ? null : URI.create(studyGraphUri);
  }

  public void setStudyGraphUri(URI studyGraphUri) {
    this.studyGraphUri = studyGraphUri == null ? null : studyGraphUri.toString();
}

  public List<Outcome> getSelectedOutcomes() {
    return selectedOutcomes == null ? Collections.EMPTY_LIST : Collections.unmodifiableList(new ArrayList<>(selectedOutcomes));
  }

  public void updateSelectedOutcomes(List<Outcome> newOutcomes){
    Utils.updateSet(this.selectedOutcomes, new HashSet<>(newOutcomes));
  }

  @JsonRawValue
  public String getProblem() {
    return problem;
  }

  @JsonDeserialize(using = ObjectToStringDeserializer.class)
  public void setProblem(String problem) {
    this.problem = problem;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SingleStudyBenefitRiskAnalysis that = (SingleStudyBenefitRiskAnalysis) o;

    if (problem != null ? !problem.equals(that.problem) : that.problem != null) return false;
    if (studyGraphUri != null ? !studyGraphUri.equals(that.studyGraphUri) : that.studyGraphUri != null) return false;
    return selectedOutcomes.equals(that.selectedOutcomes);

  }

  @Override
  public int hashCode() {
    int result = problem != null ? problem.hashCode() : 0;
    result = 31 * result + (studyGraphUri != null ? studyGraphUri.hashCode() : 0);
    result = 31 * result + selectedOutcomes.hashCode();
    return result;
  }
}
package org.drugis.addis.analyses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.drugis.addis.interventions.Intervention;
import org.drugis.addis.outcomes.Outcome;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by daan on 24-2-16.
 */
@Entity
public class MetaBenefitRiskAnalysis extends AbstractAnalysis implements Serializable {
  @Id
  @SequenceGenerator(name = "analysis_sequence", sequenceName = "shared_analysis_id_seq", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "analysis_sequence")
  private Integer id;

  private Integer projectId;

  private String title;

  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinTable(name = "MetaBenefitRiskAnalysis_Alternative",
          joinColumns = {@JoinColumn(name = "analysisId", referencedColumnName = "id")},
          inverseJoinColumns = {@JoinColumn(name = "alternativeId", referencedColumnName = "id")})
  private List<Intervention> includedAlternatives = new ArrayList<>();

  public MetaBenefitRiskAnalysis() {
  }

  public MetaBenefitRiskAnalysis(Integer id, Integer projectId, String title, List<Intervention> includedAlternatives) {
    this.id = id;
    this.projectId = projectId;
    this.title = title;
    this.includedAlternatives = includedAlternatives;
  }

  @Override
  public Integer getProjectId() {
    return null;
  }

  public Integer getId() {
    return id;
  }

  public List<Intervention> getIncludedAlternatives() {
    return includedAlternatives;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    MetaBenefitRiskAnalysis that = (MetaBenefitRiskAnalysis) o;

    return id != null ? id.equals(that.id) : that.id == null;

  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }

//  private class OutcomeInclusion {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @JsonIgnore
//    private Integer id;
//
//    @ManyToOne()
//    @JoinColumn(name="analysisId")
//    private NetworkMetaAnalysis networkMetaAnalysis;
//
//    private Integer outcomeId;
//    private Integer modelId;
//  }
}

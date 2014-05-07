package org.drugis.addis.analyses;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.util.ObjectToStringDeserializer;

import javax.persistence.*;

/**
 * Created by connor on 6-5-14.
 */
@Entity
@JsonTypeName("Network meta-analysis")
public class NetworkMetaAnalysis extends AbstractAnalysis {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;
  private Integer projectId;
  private String name;

  @JsonRawValue
  private String problem;

  @ManyToOne(targetEntity = Outcome.class)
  @JoinColumn(name = "outcomeId")
  private Outcome outcome;

  public NetworkMetaAnalysis() {
  }

  public NetworkMetaAnalysis(Integer id, Integer projectId, String name, Outcome outcome, String problem) {
    this.id = id;
    this.projectId = projectId;
    this.name = name;
    this.outcome = outcome;
    this.problem = problem;
  }

  public NetworkMetaAnalysis(Integer projectId, String name) {
    this.projectId = projectId;
    this.name = name;
  }

  public NetworkMetaAnalysis(Integer id, Integer projectId, String name) {
    this(id, projectId, name, null, null);
  }

  public Integer getId() {
    return id;
  }

  public Integer getProjectId() {
    return projectId;
  }

  public String getName() {
    return name;
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
    if (!(o instanceof NetworkMetaAnalysis)) return false;

    NetworkMetaAnalysis that = (NetworkMetaAnalysis) o;

    if (id != null ? !id.equals(that.id) : that.id != null) return false;
    if (!name.equals(that.name)) return false;
    if (outcome != null ? !outcome.equals(that.outcome) : that.outcome != null) return false;
    if (problem != null ? !problem.equals(that.problem) : that.problem != null) return false;
    if (!projectId.equals(that.projectId)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + projectId.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + (problem != null ? problem.hashCode() : 0);
    result = 31 * result + (outcome != null ? outcome.hashCode() : 0);
    return result;
  }
}
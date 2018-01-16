package org.drugis.addis.subProblems;

import com.fasterxml.jackson.annotation.JsonRawValue;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Created by joris on 8-5-17.
 */
@Entity
public class SubProblem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private Integer workspaceId;

  @JsonRawValue
  private String definition;

  private String title;

  public SubProblem() {
  }

  public SubProblem(Integer id, Integer workspaceId, String definition, String title) {
    this.id = id;
    this.workspaceId = workspaceId;
    this.definition = definition;
    this.title = title;
  }

  public SubProblem(Integer workspaceId, String definition, String title) {
    this(null, workspaceId, definition, title);
  }

  public Integer getId() {
    return id;
  }

  public Integer getWorkspaceId() {
    return workspaceId;
  }

  public String getDefinition() {
    return definition;
  }

  public String getTitle() {
    return title;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SubProblem that = (SubProblem) o;

    if (id != null ? !id.equals(that.id) : that.id != null) return false;
    if (!workspaceId.equals(that.workspaceId)) return false;
    if (!definition.equals(that.definition)) return false;
    return title.equals(that.title);
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + workspaceId.hashCode();
    result = 31 * result + definition.hashCode();
    result = 31 * result + title.hashCode();
    return result;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setDefinition(String definition) {
    this.definition = definition;
  }
}

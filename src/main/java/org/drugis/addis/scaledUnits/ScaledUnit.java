package org.drugis.addis.scaledUnits;

import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.net.URI;
import java.util.Objects;

/**
 * Created by joris on 19-4-17.
 */
@Entity
public class ScaledUnit {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private Integer projectId;
  @Type(type = "org.drugis.addis.util.UriUserType")
  private URI conceptUri;
  private Double multiplier;
  private String name;

  public ScaledUnit() {
  }

  public ScaledUnit(Integer projectId, URI conceptUri, Double multiplier, String name) {
    this.projectId = projectId;
    this.conceptUri = conceptUri;
    this.multiplier = multiplier;
    this.name = name;
  }
  public ScaledUnit(Integer id,Integer projectId, URI conceptUri, Double multiplier, String name) {
    this.id = id;
    this.projectId = projectId;
    this.conceptUri = conceptUri;
    this.multiplier = multiplier;
    this.name = name;
  }

  public Integer getId() {
    return id;
  }

  public Integer getProjectId() {
    return projectId;
  }

  public URI getConceptUri() {
    return conceptUri;
  }

  public Double getMultiplier() {
    return multiplier;
  }

  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ScaledUnit that = (ScaledUnit) o;
    return Objects.equals(id, that.id) && Objects.equals(projectId, that.projectId) && Objects.equals(conceptUri, that.conceptUri) && Objects.equals(multiplier, that.multiplier) && Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, projectId, conceptUri, multiplier, name);
  }
}

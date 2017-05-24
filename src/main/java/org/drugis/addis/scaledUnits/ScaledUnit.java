package org.drugis.addis.scaledUnits;

import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.net.URI;

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

    if (id != null ? !id.equals(that.id) : that.id != null) return false;
    if (!projectId.equals(that.projectId)) return false;
    if (!conceptUri.equals(that.conceptUri)) return false;
    if (!multiplier.equals(that.multiplier)) return false;
    return name.equals(that.name);
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + projectId.hashCode();
    result = 31 * result + conceptUri.hashCode();
    result = 31 * result + multiplier.hashCode();
    result = 31 * result + name.hashCode();
    return result;
  }
}

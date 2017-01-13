package org.drugis.addis.interventions.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.drugis.addis.interventions.controller.viewAdapter.AbstractInterventionViewAdapter;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.PrimaryKeyJoinColumn;
import java.net.URI;

/**
 * Created by connor on 31-5-16.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = SimpleIntervention.class, name = "simple"),
        @JsonSubTypes.Type(value = FixedDoseIntervention.class, name = "fixed"),
        @JsonSubTypes.Type(value = TitratedDoseIntervention.class, name = "titrated"),
        @JsonSubTypes.Type(value = BothDoseTypesIntervention.class, name = "both")})
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@PrimaryKeyJoinColumn(name = "singleInterventionId", referencedColumnName = "id")
public abstract class SingleIntervention extends AbstractIntervention {

  private String semanticInterventionUri;
  private String semanticInterventionLabel;

  public SingleIntervention() {
  }

  public SingleIntervention(Integer id, Integer project, String name, String motivation, URI semanticInterventionUri,
                            String semanticInterventionLabel) {
    super(id, project, name, motivation);
    this.semanticInterventionUri = semanticInterventionUri.toString();
    this.semanticInterventionLabel = semanticInterventionLabel;
  }

  public SingleIntervention(Integer project, String name, String motivation, URI semanticInterventionUri,
                            String semanticInterventionLabel) {
    this(null, project, name, motivation, semanticInterventionUri, semanticInterventionLabel);
  }

  public URI getSemanticInterventionUri() {
    return URI.create(semanticInterventionUri);
  }

  public String getSemanticInterventionLabel() {
    return semanticInterventionLabel;
  }

  @Override
  public abstract AbstractInterventionViewAdapter toViewAdapter();

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    SingleIntervention that = (SingleIntervention) o;

    if (!semanticInterventionUri.equals(that.semanticInterventionUri)) return false;
    return semanticInterventionLabel != null ? semanticInterventionLabel.equals(that.semanticInterventionLabel) : that.semanticInterventionLabel == null;

  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + semanticInterventionUri.hashCode();
    result = 31 * result + (semanticInterventionLabel != null ? semanticInterventionLabel.hashCode() : 0);
    return result;
  }
}

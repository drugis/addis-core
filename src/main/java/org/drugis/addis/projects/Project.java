package org.drugis.addis.projects;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.drugis.addis.security.Account;
import org.drugis.addis.util.URIStringConverter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;
import java.net.URI;
import java.util.Date;
import java.util.Objects;

/**
 * Created by daan on 2/6/14.
 */
@Entity
public class Project implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(targetEntity = Account.class)
  @JoinColumn(name = "owner")
  private Account owner;

  private String name;

  private String description;

  private String namespaceUid;

  @Convert(converter = URIStringConverter.class)
  private URI datasetVersion;

  private Boolean isArchived = false;

  @Column(name = "archived_on")
  @Type(type = "date")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  private Date archivedOn;

  public Project() {
  }

  public Project(Integer id, Account owner, String name, String description, String namespaceUid, URI datasetVersion) {
    this.id = id;
    this.owner = owner;
    this.name = name;
    this.description = description;
    this.namespaceUid = namespaceUid;
    this.datasetVersion = datasetVersion;
  }

  public Project(Account owner, String name, String description, String namespaceUid, URI datasetVersion) {
    this.owner = owner;
    this.name = name;
    this.description = description;
    this.namespaceUid = namespaceUid;
    this.datasetVersion = datasetVersion;
  }

  public Integer getId() {
    return id;
  }

  public Account getOwner() {
    return owner;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getNamespaceUid() {
    return namespaceUid;
  }

  public URI getDatasetVersion() {
    return datasetVersion;
  }

  public Boolean getArchived() {
    return isArchived;
  }

  Date getArchivedOn() {
    return archivedOn;
  }

  public void setArchived(Boolean isArchived) {
    this.isArchived = isArchived;
  }

  public void setArchivedOn(Date archivedOn) {
    this.archivedOn = archivedOn;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Project project = (Project) o;
    return Objects.equals(id, project.id) &&
        Objects.equals(owner, project.owner) &&
        Objects.equals(name, project.name) &&
        Objects.equals(description, project.description) &&
        Objects.equals(namespaceUid, project.namespaceUid) &&
        Objects.equals(datasetVersion, project.datasetVersion) &&
        Objects.equals(isArchived, project.isArchived) &&
        Objects.equals(archivedOn, project.archivedOn);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, owner, name, description, namespaceUid, datasetVersion, isArchived, archivedOn);
  }

  public ProjectCommand getCommand() {
    return new ProjectCommand(this.getName(), this.getDescription(), this.getNamespaceUid(), this.getDatasetVersion());
  }
}

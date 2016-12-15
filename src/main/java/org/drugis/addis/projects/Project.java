package org.drugis.addis.projects;

import org.drugis.addis.security.Account;
import org.drugis.addis.util.URIStringConverter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;
import java.net.URI;
import java.util.Date;

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

  public Date getArchivedOn() {
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

    if (!id.equals(project.id)) return false;
    if (!owner.equals(project.owner)) return false;
    if (!name.equals(project.name)) return false;
    if (!description.equals(project.description)) return false;
    if (!namespaceUid.equals(project.namespaceUid)) return false;
    if (!datasetVersion.equals(project.datasetVersion)) return false;
    if (!isArchived.equals(project.isArchived)) return false;
    return archivedOn != null ? archivedOn.equals(project.archivedOn) : project.archivedOn == null;
  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + owner.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + description.hashCode();
    result = 31 * result + namespaceUid.hashCode();
    result = 31 * result + datasetVersion.hashCode();
    result = 31 * result + isArchived.hashCode();
    result = 31 * result + (archivedOn != null ? archivedOn.hashCode() : 0);
    return result;
  }

  public ProjectCommand getCommand() {
    return new ProjectCommand(this.getName(), this.getDescription(), this.getNamespaceUid(), this.getDatasetVersion());
  }
}

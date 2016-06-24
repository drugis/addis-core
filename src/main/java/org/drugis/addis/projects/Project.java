package org.drugis.addis.projects;

import org.drugis.addis.security.Account;

import javax.persistence.*;
import java.io.Serializable;

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

  @Column
  private String name;

  @Column
  private String description;

  @Column
  private String namespaceUid;

  @Column
  private String datasetVersion;

  public Project() {
  }

  public Project(Integer id, Account owner, String name, String description, String namespaceUid, String datasetVersion) {
    this.id = id;
    this.owner = owner;
    this.name = name;
    this.description = description;
    this.namespaceUid = namespaceUid;
    this.datasetVersion = datasetVersion;
  }

  public Project(Account owner, String name, String description, String namespaceUid, String datasetVersion) {
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

  public String getDatasetVersion() {
    return datasetVersion;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Project project = (Project) o;

    if (!datasetVersion.equals(project.datasetVersion)) return false;
    if (description != null ? !description.equals(project.description) : project.description != null) return false;
    if (id != null ? !id.equals(project.id) : project.id != null) return false;
    if (!name.equals(project.name)) return false;
    if (!namespaceUid.equals(project.namespaceUid)) return false;
    if (!owner.equals(project.owner)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + owner.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + (description != null ? description.hashCode() : 0);
    result = 31 * result + namespaceUid.hashCode();
    result = 31 * result + datasetVersion.hashCode();
    return result;
  }
}

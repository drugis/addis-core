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


  public Project() {
  }

  public Project(Integer id, Account owner, String name, String description, String namespaceUid) {
    this.id = id;
    this.owner = owner;
    this.name = name;
    this.description = description;
    this.namespaceUid = namespaceUid;
  }

  public Project(Account owner, String name, String description, String namespaceUid) {
    this.owner = owner;
    this.name = name;
    this.description = description;
    this.namespaceUid = namespaceUid;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Account getOwner() {
    return owner;
  }

  public void setOwner(Account owner) {
    this.owner = owner;
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

  public void setNamespaceUid(String namespaceUid) {
    this.namespaceUid = namespaceUid;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Project project = (Project) o;

    if (!description.equals(project.description)) return false;
    if (id != null ? !id.equals(project.id) : project.id != null) return false;
    if (!name.equals(project.name)) return false;
    if (!owner.equals(project.owner)) return false;
    if (!namespaceUid.equals(project.namespaceUid)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + owner.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + description.hashCode();
    result = 31 * result + namespaceUid.hashCode();
    return result;
  }
}

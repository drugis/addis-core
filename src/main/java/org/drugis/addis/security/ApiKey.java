package org.drugis.addis.security;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by daan on 15-9-15.
 */
@Entity
@Table(name = "ApplicationKey")
public class ApiKey {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private String secretKey;
  private Integer accountId;
  private String applicationName;
  @Column(columnDefinition = "DATE")
  private Date creationDate;
  @Column(columnDefinition = "DATE")
  private Date revocationDate;

  public ApiKey() {
  }

  public ApiKey(Integer id, String secretKey, Integer accountId, String applicationName, Date creationDate, Date revocationDate) {
    this.id = id;
    this.secretKey = secretKey;
    this.accountId = accountId;
    this.applicationName = applicationName;
    this.creationDate = creationDate;
    this.revocationDate = revocationDate;
  }

  public Integer getId() {
    return id;
  }

  public String getSecretkey() {
    return secretKey;
  }

  public Integer getAccountId() {
    return accountId;
  }

  public String getApplicationName() {
    return applicationName;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public Date getRevocationDate() {
    return revocationDate;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ApiKey apiKey = (ApiKey) o;

    if (!id.equals(apiKey.id)) return false;
    if (!secretKey.equals(apiKey.secretKey)) return false;
    if (!accountId.equals(apiKey.accountId)) return false;
    if (!applicationName.equals(apiKey.applicationName)) return false;
    if (!creationDate.equals(apiKey.creationDate)) return false;
    return revocationDate.equals(apiKey.revocationDate);

  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + secretKey.hashCode();
    result = 31 * result + accountId.hashCode();
    result = 31 * result + applicationName.hashCode();
    result = 31 * result + creationDate.hashCode();
    result = 31 * result + revocationDate.hashCode();
    return result;
  }
}

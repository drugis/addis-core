package org.drugis.addis.ordering;

import com.fasterxml.jackson.annotation.JsonRawValue;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Ordering {
  @Id
  private Integer workspaceId;

  @JsonRawValue
  private String ordering;

  public Ordering() {
  }

  public String getOrdering() {
    return ordering;
  }
}

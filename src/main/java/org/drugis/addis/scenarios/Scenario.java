package org.drugis.addis.scenarios;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by connor on 3-4-14.
 */
@Entity
public class Scenario {
  @Id
  private Integer id;

  public Scenario() {
  }

  public Scenario(Integer id) {
    this.id = id;
  }

  public Integer getId() {
    return id;
  }
}

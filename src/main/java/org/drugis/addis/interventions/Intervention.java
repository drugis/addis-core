package org.drugis.addis.interventions;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Created by daan on 3/6/14.
 */
@Entity
public class Intervention {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;
  private String name;
  private String motivation;
  private String semanticInterventionLabel;
  private String semanticInterventionUrl;
}

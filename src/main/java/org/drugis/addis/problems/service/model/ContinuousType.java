package org.drugis.addis.problems.service.model;

/**
 * Created by daan on 3/26/14.
 */
public class ContinuousType extends DistributionType {
  @Override
  public String getType() {
    return "dnormal";
  }
}

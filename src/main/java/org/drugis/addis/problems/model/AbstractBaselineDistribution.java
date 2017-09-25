package org.drugis.addis.problems.model;

/**
 * Created by daan on 3-3-17.
 */

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = NormalBaselineDistribution.class, name = "dnorm"),
        @JsonSubTypes.Type(value = BetaLogitBaselineDistribution.class, name="dbeta-logit"),
        @JsonSubTypes.Type(value = BetaCloglogBaselineDistribution.class, name="dbeta-cloglog"),
        @JsonSubTypes.Type(value = StudentTBaselineDistribution.class, name="dt"),
        @JsonSubTypes.Type(value = SurvivalBaselineDistribution.class, name="dsurv")})
public abstract class AbstractBaselineDistribution {
  protected String name;
  protected String type;
  protected String scale;

  public AbstractBaselineDistribution(String name, String type, String scale) {
    this.name = name;
    this.type = type;
    this.scale = scale;
  }

  public AbstractBaselineDistribution() {
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public String getScale() {
    return scale;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AbstractBaselineDistribution that = (AbstractBaselineDistribution) o;

    if (!name.equals(that.name)) return false;
    if (!type.equals(that.type)) return false;
    return scale.equals(that.scale);
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + type.hashCode();
    result = 31 * result + scale.hashCode();
    return result;
  }
}

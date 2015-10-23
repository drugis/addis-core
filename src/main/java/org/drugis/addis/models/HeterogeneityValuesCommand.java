package org.drugis.addis.models;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Created by daan on 22-10-15.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = StdDevValuesCommand.class, name = "standard-deviation"),
        @JsonSubTypes.Type(value = VarianceValuesCommand.class, name = "variance"),
        @JsonSubTypes.Type(value = PrecisionValuesCommand.class, name = "precision")})
public abstract class HeterogeneityValuesCommand {
}

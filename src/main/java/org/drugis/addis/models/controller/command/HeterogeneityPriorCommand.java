package org.drugis.addis.models.controller.command;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Created by daan on 22-10-15.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = StdDevHeterogeneityPriorCommand.class, name = "standard-deviation"),
        @JsonSubTypes.Type(value = VarianceHeterogeneityPriorCommand.class, name = "variance"),
        @JsonSubTypes.Type(value = PrecisionHeterogeneityPriorCommand.class, name = "precision"),
        @JsonSubTypes.Type(value = StdDevHeterogeneityPriorCommand.class, name="automatic")})
public abstract class HeterogeneityPriorCommand {
}

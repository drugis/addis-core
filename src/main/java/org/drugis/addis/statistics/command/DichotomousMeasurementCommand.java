package org.drugis.addis.statistics.command;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.drugis.addis.statistics.exception.MissingMeasurementException;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by daan on 20-1-17.
 */
public class DichotomousMeasurementCommand extends AbstractMeasurementCommand {
  Map<String, Double> resultProperties = new HashMap<>();

  public DichotomousMeasurementCommand() {
  }

  public DichotomousMeasurementCommand(URI endpointUri, URI armUri, Map<String, Double> resultProperties) {
    super(endpointUri, armUri);
    this.resultProperties = resultProperties;
  }

  public Map<String, Double> getResultProperties() {
    return resultProperties;
  }

  @JsonIgnore
  public Integer getCount() throws MissingMeasurementException {
    Double count = resultProperties.get("count");
    if (count == null) {
      throw new MissingMeasurementException("missing count");
    }
    return count.intValue();
  }

  @JsonIgnore
  public Integer getSampleSize() throws MissingMeasurementException {
    Double sampleSize = resultProperties.get("sampleSize");
    if (sampleSize == null) {
      throw new MissingMeasurementException("missing sample size");
    }
    return sampleSize.intValue();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    DichotomousMeasurementCommand that = (DichotomousMeasurementCommand) o;

    return resultProperties.equals(that.resultProperties);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + resultProperties.hashCode();
    return result;
  }
}

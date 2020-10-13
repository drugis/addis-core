package org.drugis.addis.statistics.command;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.drugis.addis.statistics.exception.MissingMeasurementException;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class ContinuousMeasurementCommand extends AbstractMeasurementCommand {
  Map<String, Double> resultProperties = new HashMap<>();

  public ContinuousMeasurementCommand(){}

  public ContinuousMeasurementCommand(URI endpointUri, URI armUri, Map<String, Double> resultProperties) {
    super(endpointUri, armUri);
    this.resultProperties = resultProperties;
  }

  public Map<String, Double> getResultProperties() {
    return resultProperties;
  }

  @JsonIgnore
  public Double getMean() throws MissingMeasurementException {
    Double mean = resultProperties.get("mean");
    if(mean == null) {
      throw new MissingMeasurementException("missing mean");
    }
    return mean;
  }

  @JsonIgnore
  public Integer getSampleSize() throws MissingMeasurementException {
    Double sampleSize = resultProperties.get("sampleSize");
    if(sampleSize == null) {
      throw new MissingMeasurementException("missing sample size");
    }
    return sampleSize.intValue();
  }

  @JsonIgnore
  public Double getStdDev() throws MissingMeasurementException {
    Double stdDev = resultProperties.get("standardDeviation");
    if(stdDev == null) {
      Double stdErr = resultProperties.get("standardError");
      Integer sampleSize = getSampleSize();
      if(stdErr != null && sampleSize != null) {
        stdDev = stdErr * Math.sqrt(sampleSize);
      }
    }
    if(stdDev == null) {
      throw new MissingMeasurementException("missing standard deviation");
    }
    return stdDev;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    ContinuousMeasurementCommand that = (ContinuousMeasurementCommand) o;

    return resultProperties.equals(that.resultProperties);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + resultProperties.hashCode();
    return result;
  }
}

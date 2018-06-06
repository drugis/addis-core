package org.drugis.addis.problems.model;

import java.net.URI;
import java.util.List;

public class DataSourceEntry {
  private final List<Double> scale;
  private final PartialValueFunction partialValueFunction;
  private final String source;
  private final URI sourceLink;

  public DataSourceEntry(List<Double> scale, PartialValueFunction partialValueFunction, String source, URI sourceLink) {
    this.scale = scale;
    this.partialValueFunction = partialValueFunction;
    this.source = source;
    this.sourceLink = sourceLink;
  }

  public DataSourceEntry(String source, URI sourceLink) {
    this(null, null, source, sourceLink);
  }

  public String getSource() {
    return source;
  }

  public URI getSourceLink() {
    return sourceLink;
  }

  public List<Double> getScale() {
    return scale;
  }

  public PartialValueFunction getPartialValueFunction() {
    return partialValueFunction;
  }
}

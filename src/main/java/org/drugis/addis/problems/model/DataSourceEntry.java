package org.drugis.addis.problems.model;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class DataSourceEntry {
  private final List<Double> scale;
  private final PartialValueFunction partialValueFunction;
  private final String source;
  private final URI sourceLink;
  private String id;

  public DataSourceEntry(String id, List<Double> scale, PartialValueFunction partialValueFunction, String source, URI sourceLink) {
    this.id = id;
    this.scale = scale;
    this.partialValueFunction = partialValueFunction;
    this.source = source;
    this.sourceLink = sourceLink;
  }

  public DataSourceEntry(String id, String source, URI sourceLink) {
    this(id, null, null, source, sourceLink);
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

  public String getId() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DataSourceEntry that = (DataSourceEntry) o;
    return Objects.equals(scale, that.scale) &&
        Objects.equals(partialValueFunction, that.partialValueFunction) &&
        Objects.equals(source, that.source) &&
        Objects.equals(sourceLink, that.sourceLink) &&
        Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {

    return Objects.hash(scale, partialValueFunction, source, sourceLink, id);
  }
}
